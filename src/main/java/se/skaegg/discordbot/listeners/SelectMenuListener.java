package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.MovieSearch;
import se.skaegg.discordbot.handlers.Timer;
import se.skaegg.discordbot.jpa.TimerRepository;

@Component
public class SelectMenuListener {

    @Value("${omdb.api.token}")
    String apiToken;

    @Autowired
    TimerRepository timerRepository;


    public SelectMenuListener(GatewayDiscordClient client) {
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
            return new Timer(timerRepository).showTimer(event);
        }
        else {
            return Mono.empty();
        }
    }
}

