package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;
import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

import java.time.Duration;
import java.util.concurrent.TimeoutException;



public class TriviaTempListener {



    public Mono<Void> createTempListener(Trivia trivia, GatewayDiscordClient client) {

//        Trivia trivia = new Trivia(triviaQuestionsRepository, triviaScoresRepository, client);

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
