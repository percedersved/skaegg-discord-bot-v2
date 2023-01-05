package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class TriviaTempListener {



    public Mono<Void> createTempListener(Trivia trivia, GatewayDiscordClient client) {

        client.on(ButtonInteractionEvent.class, buttonEvent -> {
                    if (buttonEvent.getCustomId().contains("trivia_")) {
                        return trivia.checkAnswer(buttonEvent);
                    }
                    else {
                        return Mono.empty();
                    }
                })
                .timeout(Duration.ofMinutes(30L))
                .onErrorResume(TimeoutException.class, ignore -> Mono.empty())
                .subscribe();
        return Mono.empty();
    }
}
