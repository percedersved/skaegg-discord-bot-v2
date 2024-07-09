package se.skaegg.discordbot.listener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.MovieSearch;
import se.skaegg.discordbot.handler.Poll;
import se.skaegg.discordbot.handler.Timer;
import se.skaegg.discordbot.repository.PollAlternativesRepository;
import se.skaegg.discordbot.repository.PollVotesRepository;
import se.skaegg.discordbot.repository.PollsRepository;
import se.skaegg.discordbot.repository.TimerRepository;

@Component
public class SelectMenuListener {

    @Value("${omdb.api.token}")
    String apiToken;

    @Autowired
    TimerRepository timerRepository;
    @Autowired
    PollsRepository pollsRepository;
    @Autowired
    PollAlternativesRepository pollAlternativesRepository;
    @Autowired
    PollVotesRepository pollVotesRepository;


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
        else if (event.getCustomId().equals("polls")) {
            return new Poll(pollsRepository, pollAlternativesRepository, pollVotesRepository).showPoll(event);
        }
        else {
            return Mono.empty();
        }
    }
}

