package se.skaegg.discordbot.listener;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.VcSubscriptionUser;
import se.skaegg.discordbot.repository.VcSubscriptionUsersRepository;

import java.util.List;

@Component
public class VcEventListener {

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

        VoiceChannel vc = newState.getChannel().block();
        // If newState dosen't have a channel it means that this event was someone leaving. Ignore this.
        if (vc == null) {
            return Mono.empty();
        }

        List<String> usersInVc = vc.getVoiceStates()
                .flatMap(VoiceState::getMember)
                .map(discord4j.core.object.entity.Member::getDisplayName)
                .collectList()
                .block();

        String usersList = String.join(", ", usersInVc);

        String embedDescription = String.format("Någon joinade voicekanal: **%s**. Just nu är **%s** i kanalen", vc.getName(), usersList);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Voicekanal :speaker:")
                .description(embedDescription)
                .build();

        if (oldState == null || !newState.getChannelId().equals(oldState.getChannelId())) {
            return newState.getChannel()
                    .flatMap(channel -> Mono.when(membersInDb.stream()
                            .map(member -> client.getUserById(Snowflake.of(member.getMemberId()))
                                    .flatMap(User::getPrivateChannel)
                                    .flatMap(privateChannel -> privateChannel.createMessage(embed))
                            )
                            .toArray(Mono[]::new)
                        )
                    );
        }
        return Mono.empty();
    }
}
