package se.skaegg.discordbot.jpa;

import java.time.LocalDate;

public class TriviaPercentageForDateEntity {

    LocalDate questionDate;
    String question;
    Double percentCorrect;

    public TriviaPercentageForDateEntity(LocalDate questionDate, String question, Double percentCorrect) {
        this.questionDate = questionDate;
        this.question = question;
        this.percentCorrect = percentCorrect;
    }


    public LocalDate getQuestionDate() {
        return questionDate;
    }

    public void setQuestionDate(LocalDate questionDate) {
        this.questionDate = questionDate;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Double getPercentCorrect() {
        return percentCorrect;
    }

    public void setPercentCorrect(Double percentCorrect) {
        this.percentCorrect = percentCorrect;
    }
}
