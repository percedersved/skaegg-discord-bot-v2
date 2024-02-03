package se.skaegg.discordbot.jpa;

public class EmojiStatsCount {

    String name;
    Long countId;

    public EmojiStatsCount(String name, Long countId) {
        this.name = name;
        this.countId = countId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCountId() {
        return countId;
    }

    public void setCountId(Long countId) {
        this.countId = countId;
    }
}
