package se.skaegg.discordbot.jpa;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trivia_scores")
public class TriviaScoresEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false, referencedColumnName = "id")
    TriviaQuestionsEntity question;

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

    public TriviaQuestionsEntity getQuestion() {
        return question;
    }

    public void setQuestion(TriviaQuestionsEntity question) {
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
