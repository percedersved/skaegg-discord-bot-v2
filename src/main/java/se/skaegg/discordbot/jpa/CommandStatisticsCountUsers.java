package se.skaegg.discordbot.jpa;

public class CommandStatisticsCountUsers {
    String user;
    Long countId;

    public CommandStatisticsCountUsers(String user, Long countId) {
        this.user = user;
        this.countId = countId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getCountId() {
        return countId;
    }

    public void setCountId(Long countId) {
        this.countId = countId;
    }
}
