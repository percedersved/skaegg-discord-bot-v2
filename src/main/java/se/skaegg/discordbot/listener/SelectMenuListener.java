package se.skaegg.discordbot.listener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.MovieSearch;
import se.skaegg.discordbot.handler.Poll;
import se.skaegg.discordbot.handler.Timer;

@Component
public class SelectMenuListener {

    @Value("${omdb.api.token}")
    String apiToken;

    MovieSearch movieSearch;
    Timer timer;
    Poll poll;

    public SelectMenuListener(GatewayDiscordClient client,
                              MovieSearch movieSearch,
                              Timer timer,
                              Poll poll) {
        this.movieSearch = movieSearch;
        this.timer = timer;
        this.poll = poll;
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    public Mono<Message> handle(SelectMenuInteractionEvent event) {

        if (event.getCustomId().equals("movies")) {
            // Defer reply to give the bot some time to get the movie from OMDB
            event.deferReply()
                    .subscribe();

            return new MovieSearch().getMovie(event, apiToken);
        }
        else if (event.getCustomId().equals("timers")) {
            return timer.showTimer(event);
        }
        else if (event.getCustomId().equals("polls")) {
            return poll.showPoll(event);
        }
        else {
            return Mono.empty();
        }
    }
}