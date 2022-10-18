package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import io.netty.handler.timeout.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;
import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

import java.time.Duration;

@Component
public class ButtonListener {

    @Autowired
    TriviaQuestionsRepository triviaQuestionsRepository;
    @Autowired
    TriviaScoresRepository triviaScoresRepository;
    @Autowired
    GatewayDiscordClient client;

    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;

    public ButtonListener(GatewayDiscordClient client) {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {

//        if (event.getCustomId().contains("trivia_")) {
//            return new Trivia(triviaQuestionsRepository, triviaScoresRepository, client).checkAnswer(event);
//        }
        if (event.getCustomId().equals("getTodaysQuestion")){
            return new Trivia(triviaQuestionsRepository, triviaScoresRepository, client).createQuestions(url, queryParams, event);
        }
        else {
            return Mono.empty();
        }
    }
}
