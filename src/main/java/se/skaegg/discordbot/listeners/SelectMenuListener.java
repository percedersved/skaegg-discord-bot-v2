package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.MovieSearch;

@Component
public class SelectMenuListener {

    @Value("${omdb.api.token}")
    String apiToken;


    public SelectMenuListener(GatewayDiscordClient client) {
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    public Mono<Message> handle(SelectMenuInteractionEvent event) {

        if (event.getCustomId().equals("movies")) {
            // Defer reply to give the bot some time to get the movie from OMDB
            event.deferReply().subscribe();
            return new MovieSearch().getMovie(event, apiToken);
        }
            else {
                return Mono.empty();
            }
    }

}

