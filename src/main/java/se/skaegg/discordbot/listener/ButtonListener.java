package se.skaegg.discordbot.listener;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.Poll;
import se.skaegg.discordbot.handler.Trivia;

@Component
public class ButtonListener {

    @Value("${trivia.source}")
    String source;
    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;

    Trivia trivia;
    Poll poll;

    public ButtonListener(GatewayDiscordClient client,
                          Trivia trivia,
                          Poll poll) {
        this.trivia = trivia;
        this.poll = poll;
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(ButtonInteractionEvent event) {

        String buttonId = event.getCustomId();
        Pattern p = Pattern.compile("(.*)_(.*)");

        if (buttonId.startsWith("getQuestion_")){
            String date;
            Matcher m = p.matcher(buttonId);
            if (m.find()) {
                date = m.group(2);
                LocalDate localDate = LocalDate.parse(date);
                return trivia.createQuestions(url, queryParams, source, localDate, event);
            }
        }
        else if (buttonId.startsWith("poll_")) {
            Matcher m = p.matcher(buttonId);
            if (m.find()) {
                poll.addVote(event);
            }
        }
        return Mono.empty();
    }
}
