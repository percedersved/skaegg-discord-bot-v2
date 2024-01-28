package se.skaegg.discordbot.jpa;

import se.skaegg.discordbot.handlers.EmojiStats;

import java.time.LocalDate;
import java.util.List;

public class EmojiStatsCountPerDay {
    LocalDate date;
    String name;
    EmojiStats.emojiUseType useType;
    Long count;

    public EmojiStatsCountPerDay(LocalDate date, String name, EmojiStats.emojiUseType useType, Long count) {
        this.date = date;
        this.name = name;
        this.useType = useType;
        this.count = count;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmojiStats.emojiUseType getUseType() {
        return useType;
    }

    public void setUseType(EmojiStats.emojiUseType useType) {
        this.useType = useType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public record EmojiEntry(long msgCount, long reactCount){};
}
