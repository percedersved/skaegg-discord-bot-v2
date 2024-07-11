package se.skaegg.discordbot.handler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.client.TenorRandomClient;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class Friday implements SlashCommand{

    @Value("${tenor.api.token}")
    String token;

    @Override
    public String getName() {
        return "fredag";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        LocalDateTime startDay = LocalDate.now().atStartOfDay();
        while (startDay.getDayOfWeek() != DayOfWeek.FRIDAY) {
            startDay = startDay.plusDays(1);
        }

        Duration duration = Duration.between(LocalDateTime.now(), startDay);
        long diff = duration.toMinutes();

        String timeLeft;
        if (diff >= 1440) {
            timeLeft = diff/24/60 + " dagar, " + diff/60%24 + "h, " + diff%60 + "m";
        }
        else if (diff >= 60) {
            timeLeft = diff/60 + "h, " + diff%60 + "m";
        }
        else {
            timeLeft = diff + "m";
        }

        final String finalTimeLeft = timeLeft + " kvar till fredag <:zlatanrage:781224556429312001>";

        EmbedCreateSpec itsFridayEmbed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Det är FREDAG!!")
                .image(new TenorRandomClient(token).process("its%20friday"))
                .description("(╯°□°）╯︵ ┻━┻")
                .build();


                if (diff > 0) {
                    event.reply().withContent(finalTimeLeft).subscribe();
                    event.getReply().flatMap(msg -> msg.addReaction(ReactionEmoji.custom(Snowflake.of("781224556429312001"), "zlatanrage", false))).subscribe();
                    return Mono.empty();
                }
                 else {
                    return event.reply().withEmbeds(itsFridayEmbed);
                }
    }
}
