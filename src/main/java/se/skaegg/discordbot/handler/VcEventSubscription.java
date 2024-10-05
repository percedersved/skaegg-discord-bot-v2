package se.skaegg.discordbot.handler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.VcSubscriptionUser;
import se.skaegg.discordbot.repository.MemberRepository;
import se.skaegg.discordbot.repository.VcSubscriptionUsersRepository;

import java.time.LocalDate;

@Component
public class VcEventSubscription extends AbstractMessageHandler implements SlashCommand {

    VcSubscriptionUsersRepository vcSubscriptionUsersRepository;
    MemberRepository memberRepository;

    public VcEventSubscription(VcSubscriptionUsersRepository vcSubscriptionUsersRepository,
                               MemberRepository memberRepository) {
        this.vcSubscriptionUsersRepository = vcSubscriptionUsersRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public String getName() {
        return "voice_prenumeration";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        deferEventReply(event, true);

        ApplicationCommandInteractionOption subCommand = event.getOptions().get(0);
        String subCommandName = subCommand.getName();

        switch (subCommandName) {
            case "registrera" -> { return register(event); }
            case "avregistrera" -> { return unregister(event); }
            default -> {
                return Mono.empty();
            }
        }
    }

    private Mono<Void> register(ChatInputInteractionEvent event) {

        String serverId = getCurrentServerId(event);

        User user = event.getInteraction().getUser();
        Member member = memberRepository.findByMemberIdAndServerId(user.getId().asString(), serverId);

        String embedDescription;
        VcSubscriptionUser alreadyPresentUser = vcSubscriptionUsersRepository.findByMemberAndServerId(member, serverId);
        if (alreadyPresentUser == null) {
            embedDescription = "Du prenumererar nu på uppdatering på alla voice kanaler och får ett DM när någon ansluter";
            VcSubscriptionUser vcSubscriptionUser = new VcSubscriptionUser();
            vcSubscriptionUser.setMember(member);
            vcSubscriptionUser.setServerId(serverId);
            vcSubscriptionUser.setSubscriptionDate(LocalDate.now());

            vcSubscriptionUsersRepository.save(vcSubscriptionUser);
        } else {
            embedDescription = "Du är redan registrerad för att få uppdateringar när någon går med i en voicekanal";
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Prenumeration")
                .description(embedDescription)
                .build();

        return event.editReply().withEmbeds(embed).then();
    }

    private Mono<Void> unregister(ChatInputInteractionEvent event) {

        String serverId = getCurrentServerId(event);
        User user = event.getInteraction().getUser();
        Member member = memberRepository.findByMemberIdAndServerId(user.getId().asString(), serverId);

        String embedDescription;
        VcSubscriptionUser vcSubscriptionUser = vcSubscriptionUsersRepository.findByMemberAndServerId(member, serverId);
        if (vcSubscriptionUser == null) {
            embedDescription = "Du är inte registrerad för uppdateringar, ingen avregistrering behövs";
        } else {
            vcSubscriptionUsersRepository.delete(vcSubscriptionUser);
            embedDescription = "Du är nu avregistrerad och får inte längre uppdateringar om när någon joinar en voicekanal";
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Avregistrering")
                .description(embedDescription)
                .build();

        return event.editReply().withEmbeds(embed).then();
    }

    private String getCurrentServerId(ChatInputInteractionEvent event) {
        return event.getInteraction().getGuild()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .blockOptional()
                .orElseThrow();
    }
}
