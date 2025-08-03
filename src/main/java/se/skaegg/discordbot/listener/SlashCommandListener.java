package se.skaegg.discordbot.listener;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.CommandStatistics;
import se.skaegg.discordbot.handler.SlashCommand;

@Component
public class SlashCommandListener {

    CommandStatistics commandStatistics;

    private final Collection<SlashCommand> commands;

    public SlashCommandListener(List<SlashCommand> slashCommands,
                                CommandStatistics commandStatistics,
                                GatewayDiscordClient client) {
        this.commandStatistics = commandStatistics;

        commands = slashCommands;
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }


    public Mono<Void> handle(ChatInputInteractionEvent event) {

        //Writes the command to statistics table in the database
        commandStatistics.addStatisticsToDb(event);

        //Convert our list to a flux that we can iterate through
        return Flux.fromIterable(commands)
                //Filter out all commands that match the name this event is for
                .filter(command -> command.getName().equals(event.getCommandName()))
                //Get the first (and only) item in the flux that matches our filter
                .next()
                //Have our command class handle all logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}
