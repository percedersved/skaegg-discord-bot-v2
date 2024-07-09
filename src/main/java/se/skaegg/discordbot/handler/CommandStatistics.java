package se.skaegg.discordbot.handler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.dto.CommandStatisticsCountCommands;
import se.skaegg.discordbot.dto.CommandStatisticsCountUsers;
import se.skaegg.discordbot.repository.CommandStatisticsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class CommandStatistics implements SlashCommand {

    @Autowired
    CommandStatisticsRepository commandStatisticsRepository;

    @Value("#{'${statistics.excluded.commands}'.split(',')}")
    List<String> excludedCommands;

    public CommandStatistics(CommandStatisticsRepository commandStatisticsRepository) {
        this.commandStatisticsRepository = commandStatisticsRepository;
    }

    @Override
    public String getName() {
        return "statistik";
    }


    public void addStatisticsToDb(ChatInputInteractionEvent event) {

        Snowflake userId = Objects.requireNonNull(event.getInteraction().getUser().getId());
        Snowflake channelId = Objects.requireNonNull(event.getInteraction()
                .getChannel()
                .doOnSuccess(MessageChannel::getId)
                .block())
                .getId();

        se.skaegg.discordbot.entity.CommandStatistics commandStatistics = new se.skaegg.discordbot.entity.CommandStatistics();
        commandStatistics.setCommandName(event.getCommandName());
        commandStatistics.setCommandDateTime(LocalDateTime.now());
        commandStatistics.setCalledByUserId(userId.asString());
        commandStatistics.setOriginalChannelId(channelId.asString());

        commandStatisticsRepository.save(commandStatistics);
    }


    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        event.deferReply().subscribe();

        List<CommandStatisticsCountCommands> countPerCommand = commandStatisticsRepository.countTotalIdsByCommandName(excludedCommands);
        int numberOfCommandsToShow = Math.min(countPerCommand.size(), 3); // This is just to know what to use the looping. If there are more than 3 rows returned from DB 3 should be max

        StringBuilder commandsSb = new StringBuilder();
        for (int i = 0; i < numberOfCommandsToShow ; i++){
            commandsSb.append(i + 1);
            commandsSb.append(". ");
            commandsSb.append(countPerCommand.get(i).getCommandName());
            commandsSb.append(" - x");
            commandsSb.append(countPerCommand.get(i).getCountId());
            commandsSb.append("\n");
        }

        List<CommandStatisticsCountUsers> countPerUser = commandStatisticsRepository.countTotalIdsByUserId(excludedCommands);
        int numberOfUsersToShow = Math.min(countPerUser.size(), 3); // This is just to know what to use the looping. If there are more than 3 rows returned from DB 3 should be max

        StringBuilder usersSb = new StringBuilder();
        for (int i = 0; i < numberOfUsersToShow ; i++){
            usersSb.append(i + 1);
            usersSb.append(". <@");
            usersSb.append(countPerUser.get(i).getUser());
            usersSb.append(">");
            usersSb.append(" - x");
            usersSb.append(countPerUser.get(i).getCountId());
            usersSb.append("\n");
        }


        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Statistik")
                .addField(EmbedCreateFields.Field.of("Kommandon", commandsSb.toString(), true))
                .addField(EmbedCreateFields.Field.of("Användare", usersSb.toString(), true))
                .build();

        return event.editReply().withEmbeds(embed).then();
    }
}
