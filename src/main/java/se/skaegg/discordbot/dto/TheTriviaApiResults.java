package se.skaegg.discordbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TheTriviaApiResults {

    @JsonProperty("id")
    String id;
    @JsonProperty("category")
    String category;
    @JsonProperty("type")
    String type;
    @JsonProperty("difficulty")
    String difficulty;
    @JsonProperty("question")
    String question;
    @JsonProperty("correctAnswer")
    String correctAnswer;
    @JsonProperty("incorrectAnswers")
    List<String> incorrectAnswers;
    @JsonProperty("tags")
    List<String> tags;
    @JsonProperty("regions")
    List<String> regions;
    @JsonProperty("isNiche")
    Boolean isNiche;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        // This is to handle the-trivia-api that has type=Multiple Choice, and we want "multiple" in db
        return type.equals("Multiple Choice") ? "multiple" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getRegions() {
        return regions;
    }

    public void setRegions(List<String> regions) {
        this.regions = regions;
    }

    public Boolean getNiche() {
        return isNiche;
    }

    public void setNiche(Boolean niche) {
        isNiche = niche;
    }
}
