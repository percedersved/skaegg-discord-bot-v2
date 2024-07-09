package se.skaegg.discordbot.handler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class TeamsVoice implements SlashCommand {

    @Autowired
    GatewayDiscordClient client;

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamsVoice.class);

    @Override
    public String getName() { return "lagvoice"; }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        var players = new StringBuilder();

        // Get the guild from the event
        Guild guild = event.getInteraction().getGuild().block();

        assert guild != null;
        guild.getMembers().subscribe(member -> {
            var voiceState = member.getVoiceState().blockOptional().orElse(null);
            LOGGER.debug("se.skaegg.discordbot.handlers.TeamVoice -> Found user {}", member.getDisplayName());
            LOGGER.debug("se.skaegg.discordbot.handlers.TeamVoice -> The voiceState for this member is: {}", voiceState);
            if (voiceState != null) {
                players.append(member.getDisplayName());
                players.append(",");
            }
        });

            List<String> teamsList = Teams.setUpTeams(players.toString());
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

        if (teamRed.isBlank() || teamBlue.isBlank()) {
            return event.reply()
                    .withContent("Tyvärr, Det är ingen som är i en röstkanal och vill leka <:sadmudd:780443021267042335><:koerdittjaeklaboegrace:814187249288872016>");
        }
        else {
            return event.reply()
                    .withEmbeds(embed);
        }
    }
}
