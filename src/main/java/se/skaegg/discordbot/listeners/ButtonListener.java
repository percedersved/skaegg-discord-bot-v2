package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;
import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

@Component
public class ButtonListener {

    @Autowired
    TriviaQuestionsRepository triviaQuestionsRepository;
    @Autowired
    TriviaScoresRepository triviaScoresRepository;
    @Autowired
    GatewayDiscordClient client;

    public ButtonListener(GatewayDiscordClient client) {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {

        if (event.getCustomId().contains("trivia_")) {
            return new Trivia(triviaQuestionsRepository, triviaScoresRepository, client).checkAnswer(event);
        }
        else {
            return Mono.empty();
        }
    }
}
