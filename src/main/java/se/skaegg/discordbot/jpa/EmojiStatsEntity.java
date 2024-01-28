package se.skaegg.discordbot.jpa;

import jakarta.persistence.*;
import se.skaegg.discordbot.handlers.EmojiStats;

import java.time.LocalDate;

@Entity
@Table(name = "emoji_stats")
public class EmojiStatsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "name")
    String name;

    @Column(name = "date")
    LocalDate date;

    @Column(name = "user")
    String userId;

    @Column(name = "channel_id")
    String channelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "use_type")
    EmojiStats.emojiUseType useType;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public EmojiStats.emojiUseType getUseType() {
        return useType;
    }

    public void setUseType(EmojiStats.emojiUseType useType) {
        this.useType = useType;
    }
}
