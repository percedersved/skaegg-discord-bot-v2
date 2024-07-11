package se.skaegg.discordbot.dto;

public class TriviaScoresCountPoints {

    Long points;
    String userId;

    public TriviaScoresCountPoints(Long points, String userId) {
        this.points = points;
        this.userId = userId;
    }


    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
