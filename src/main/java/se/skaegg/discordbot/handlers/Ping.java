package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Ping implements SlashCommand {

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return event.reply()
                .withEphemeral(true)
                .withContent("Pong!")
                .onErrorResume(throwable -> event.reply()
                        .withEphemeral(true)
                        .withContent("Nu gick något fel. Testa igen vettja!"));
    }
}
