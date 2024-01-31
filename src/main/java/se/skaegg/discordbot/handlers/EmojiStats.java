package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.jpa.EmojiStatsCount;
import se.skaegg.discordbot.jpa.EmojiStatsEntity;
import se.skaegg.discordbot.jpa.EmojiStatsRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmojiStats implements SlashCommand {

    @Autowired
    EmojiStatsRepository emojiStatsRepository;

    public EmojiStats(EmojiStatsRepository emojiStatsRepository) {
        this.emojiStatsRepository = emojiStatsRepository;
    }

    @Override
    public String getName() {
        return "emojistats";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return showCountPerEmoji(event);
    }

    public void saveEmojiUsage(String customEmoji, String channelId, String userId, emojiUseType type, String emojiId) {
        EmojiStatsEntity emojiStat = new EmojiStatsEntity();
        emojiStat.setDate(LocalDate.now());
        emojiStat.setName(customEmoji);
        emojiStat.setChannelId(channelId);
        emojiStat.setUserId(userId);
        emojiStat.setUseType(type);
        emojiStat.setEmojiId(emojiId);
        emojiStatsRepository.save(emojiStat);
    }

    private Mono<Void> showCountPerEmoji(ChatInputInteractionEvent event) {
        event.deferReply().subscribe();
        List<EmojiStatsCount> dbResult = emojiStatsRepository.countTotalPerEmoji();
        int numberOfResultsToShow = Math.min(dbResult.size(), 10);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfResultsToShow ; i++) {
            EmojiStatsCount entry = dbResult.get(i);
            sb.append(i + 1);
            sb.append(". ");
            sb.append(entry.getName());
            sb.append(" - ");
            sb.append(entry.getCountId());
            sb.append("\n");
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Mest använda emojis")
                .addField(EmbedCreateFields.Field.of("Emoji - Antal", sb.toString(), true))
                .build();

        return event.createFollowup().withEmbeds(embed).then();
    }

    public enum emojiUseType {
        REACTION, MESSAGE
    }
}
