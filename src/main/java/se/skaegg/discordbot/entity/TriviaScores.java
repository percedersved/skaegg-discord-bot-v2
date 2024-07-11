package se.skaegg.discordbot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trivia_scores")
public class TriviaScores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    TriviaQuestions question;

    @Column(name = "user_id")
    String userId;

    @Column(name = "answer_date")
    LocalDate answerDate;

    @Column(name = "correct_answer")
    Boolean correctAnswer;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TriviaQuestions getQuestion() {
        return question;
    }

    public void setQuestion(TriviaQuestions question) {
        this.question = question;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(LocalDate answerDate) {
        this.answerDate = answerDate;
    }

    public Boolean getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Boolean correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
}
