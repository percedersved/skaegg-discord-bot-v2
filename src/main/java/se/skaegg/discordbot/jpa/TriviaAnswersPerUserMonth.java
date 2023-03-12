package se.skaegg.discordbot.jpa;

public class TriviaAnswersPerUserMonth {
    String userId;
    Long points;
    Float percent;


    public TriviaAnswersPerUserMonth(String userId, Long points, Float percent) {
        this.userId = userId;
        this.points = points;
        this.percent = percent;
    }

    public TriviaAnswersPerUserMonth() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getPoints() {
        return points;
    }

    public void setPoints(Long points) {
        this.points = points;
    }

    public Float getPercent() {
        return percent;
    }

    public void setPercent(Float percent) {
        this.percent = percent;
    }
}
