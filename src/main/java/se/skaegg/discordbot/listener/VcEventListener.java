package se.skaegg.discordbot.listener;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.VcSubscriptionUser;
import se.skaegg.discordbot.repository.VcSubscriptionUsersRepository;

import java.util.List;

@Component
public class VcEventListener {

    static final Logger LOG = LoggerFactory.getLogger(VcEventListener.class);

    GatewayDiscordClient client;
    VcSubscriptionUsersRepository vcSubscriptionUsersRepository;

    public VcEventListener(GatewayDiscordClient client,
                           VcSubscriptionUsersRepository vcSubscriptionUsersRepository) {
        this.client = client;
        client.on(VoiceStateUpdateEvent.class, this::handle).subscribe();
        this.vcSubscriptionUsersRepository = vcSubscriptionUsersRepository;
    }

    private Mono<Void> handle(VoiceStateUpdateEvent event) {
        String serverId = event.getCurrent().getGuildId().asString();
        List<VcSubscriptionUser> vcSubscriptionUsers = vcSubscriptionUsersRepository.findAllByServerId(serverId);

        // If we have no subscribers just return
        if (vcSubscriptionUsers == null || vcSubscriptionUsers.isEmpty()) {
            return Mono.empty();
        }

        List<Member> membersInDb = vcSubscriptionUsers.stream()
                .map(VcSubscriptionUser::getMember)
                .toList();

        var newState = event.getCurrent();
        var oldState = event.getOld().orElse(null);

        String currentUserId = newState.getUser()
                .map(User::getId)
                .map(Snowflake::asString)
                .blockOptional()
                .orElseThrow();

        VoiceChannel newStateVc = newState.getChannel().block();

        Member memberToJoinOrLeave = membersInDb.stream()
                .filter(m -> m.getMemberId()
                        .equals(currentUserId))
                .findFirst()
                .get();

        VcSubscriptionUser vcSubscriptionUser = vcSubscriptionUsersRepository.findByMemberAndServerId(memberToJoinOrLeave, serverId);

        EmbedCreateSpec embed;

        // If newState doesn't have a channel it means that this event was someone leaving.
        if (newStateVc == null && oldState != null) {
            // If the user doesn't want DMs when someone leave just return
            if (!vcSubscriptionUser.isLeaveNotice()) {
                return Mono.empty();
            }

            List<VcSubscriptionUser> usersWithLeaveNotice = vcSubscriptionUsersRepository.findByLeaveNoticeAndServerId(true, serverId);
            List<Member> membersWithLeaveNotice = usersWithLeaveNotice.stream()
                    .map(VcSubscriptionUser::getMember)
                    .toList();

            VoiceChannel oldStateVc = oldState.getChannel().block();
            List<String> usersInVc = oldStateVc.getVoiceStates()
                    .flatMap(VoiceState::getMember)
                    .map(discord4j.core.object.entity.Member::getDisplayName)
                    .collectList()
                    .block();

            String usersList;
            // No one is in the voice channel
            assert usersInVc != null;
            if (usersInVc.isEmpty()) {
                usersList = "ingen";
            } else {
                usersList = String.join(", ", usersInVc);
            }

            var embedDescription = String.format("**%s** lämnade <#%s>. Just nu är %s i kanalen",
                    memberToJoinOrLeave.getDisplayName(), oldStateVc.getId().asString(), usersList);

            embed = createEmbed(embedDescription);
            return pushMessageToMember(membersWithLeaveNotice, currentUserId, embed);

        } else {
            List<String> usersInVc = newStateVc.getVoiceStates()
                    .flatMap(VoiceState::getMember)
                    .map(discord4j.core.object.entity.Member::getDisplayName)
                    .collectList()
                    .block();

            var usersList = String.join(", ", usersInVc);

            var embedDescription = String.format("**%s** joinade <#%s>. Just nu är **%s** i kanalen",
                    memberToJoinOrLeave.getDisplayName(), newStateVc.getId().asString(), usersList);

            embed = createEmbed(embedDescription);

            if (oldState == null || !newState.getChannelId().equals(oldState.getChannelId())) {
                return pushMessageToMember(membersInDb, currentUserId, embed);
            }
        }

        return Mono.empty();
    }

    private EmbedCreateSpec createEmbed(String description) {
       return EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Voicekanal :speaker:")
                .description(description)
                .build();
    }

    private Mono<Void> pushMessageToMember(List<Member> members, String currentUserId, EmbedCreateSpec embed) {
        return Mono.when(members.stream()
                        .filter(m -> !currentUserId.equals(m.getMemberId()))
                        .map(member -> client.getUserById(Snowflake.of(member.getMemberId()))
                        .flatMap(User::getPrivateChannel)
                        .flatMap(privateChannel -> privateChannel.createMessage(embed))
                        )
                .toArray(Mono[]::new)
        );
    }
}