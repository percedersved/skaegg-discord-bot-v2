package se.skaegg.discordbot.dto;

import java.util.List;

public interface TriviaResults {
    String getCategory();
    String getType();
    String getDifficulty();
    String getQuestion();
    String getCorrectAnswer();
    List<String> getIncorrectAnswers();
}
