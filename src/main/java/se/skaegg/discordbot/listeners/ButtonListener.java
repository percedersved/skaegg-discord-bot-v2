package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.Trivia;
import se.skaegg.discordbot.jpa.TriviaButtonClicksRepository;
import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ButtonListener {

    @Autowired
    TriviaQuestionsRepository triviaQuestionsRepository;
    @Autowired
    TriviaScoresRepository triviaScoresRepository;
    @Autowired
    TriviaButtonClicksRepository triviaButtonClicksRepository;
    @Autowired
    GatewayDiscordClient client;

    @Value("${trivia.source}")
    String source;
    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;

    public ButtonListener(GatewayDiscordClient client) {
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {

        String buttonId = event.getCustomId();

        if (buttonId.startsWith("getQuestion_")){
            String date;
            Pattern p = Pattern.compile("(.*)_(.*)");
            Matcher m = p.matcher(buttonId);
            if (m.find()) {
                date = m.group(2);
                LocalDate localDate = LocalDate.parse(date);
                return new Trivia(triviaQuestionsRepository,
                        triviaScoresRepository,
                        triviaButtonClicksRepository,
                        client)
                        .createQuestions(url, queryParams, source, localDate, event);
            }
        }
        return Mono.empty();
    }
}
