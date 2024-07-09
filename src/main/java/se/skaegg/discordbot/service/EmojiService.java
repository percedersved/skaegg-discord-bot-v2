package se.skaegg.discordbot.service;

import se.skaegg.discordbot.dto.EmojiStatsCount;
import se.skaegg.discordbot.dto.EmojiStatsCountPerDay;
import se.skaegg.discordbot.entity.EmojiStats;

import java.time.LocalDate;
import java.util.List;

public interface EmojiService {

    List<EmojiStatsCount> getTotalCountPerEmoji();
    List<EmojiStats> getEmojiStats(LocalDate from, LocalDate to);
    List<EmojiStatsCountPerDay> getEmojisUsedPerDay(LocalDate from, LocalDate to, int count, int offset);
}
