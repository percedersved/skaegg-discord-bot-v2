package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriviaTempListener {



    public Mono<Void> createTempListener(Trivia trivia, GatewayDiscordClient client) {

        AtomicBoolean listenerUsed = new AtomicBoolean(false);
        client.on(ButtonInteractionEvent.class, buttonEvent -> {
                    if (buttonEvent.getCustomId().contains("trivia_") && !listenerUsed.get()) {
                        listenerUsed.set(true); // Need to set this to know if the listener has been used. If you answer multiple questions in a short time mutliple listeners will listen otherwise.
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