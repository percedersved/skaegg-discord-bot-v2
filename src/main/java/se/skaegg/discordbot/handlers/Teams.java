package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Teams implements SlashCommand {

    @Override
    public String getName() { return "lag"; }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        String players = event.getOption("deltagare")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get();

        List<String> teamsList = setUpTeams(players);
        String teamRed = teamsList.get(0);
        String teamBlue= teamsList.get(1);

        final var IMAGE_URL_TEAMS = "https://static-cdn.jtvnw.net/emoticons/v1/166266/3.0";

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .addField("----- Lag Röd -----", teamRed, true)
                .addField("----- Lag Blå -----", teamBlue, true)
                .image(IMAGE_URL_TEAMS)
                .title("Lag")
                .build();

        return event.reply()
                .withEmbeds(embed);
    }


    public static List<String> setUpTeams(String players) {

        players = players.replace(" ", "");
        List<String> playersList = Arrays.asList(players.split(",", -1));

        int numberOfPlayers = playersList.size();

        Collections.shuffle(playersList);

        List<String> teamA = new ArrayList<>(playersList.subList(0, (numberOfPlayers + 1)/2));
        List<String> teamB = new ArrayList<>(playersList.subList((numberOfPlayers + 1)/2, numberOfPlayers));

        var teamAString = teamA.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n", "", ""));

        var teamBString = teamB.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("\n", "", ""));


        return Arrays.asList(teamAString, teamBString);
    }
}
