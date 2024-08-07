package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "emoji_stats")
public class EmojiStats {
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
    se.skaegg.discordbot.handler.EmojiStats.emojiUseType useType;

    @Column(name = "emoji_id")
    String emojiId;


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

    public se.skaegg.discordbot.handler.EmojiStats.emojiUseType getUseType() {
        return useType;
    }

    public void setUseType(se.skaegg.discordbot.handler.EmojiStats.emojiUseType useType) {
        this.useType = useType;
    }

    public String getEmojiId() {
        return emojiId;
    }

    public void setEmojiId(String emojiId) {
        this.emojiId = emojiId;
    }
}
