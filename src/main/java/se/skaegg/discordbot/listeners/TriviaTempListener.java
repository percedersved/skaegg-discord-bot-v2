package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class TriviaTempListener {



    public Mono<Void> createTempListener(Trivia trivia, GatewayDiscordClient client) {

        return client.on(ButtonInteractionEvent.class, buttonEvent -> {
                    if (buttonEvent.getCustomId().contains("trivia_")) {
                        return trivia.checkAnswer(buttonEvent);
                    }
                    return Mono.empty();
                })
                .timeout(Duration.ofSeconds(15L))
                .onErrorResume(TimeoutException.class, ignore -> Mono.empty())
                .then();
    }
}
