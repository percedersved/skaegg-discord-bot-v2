package se.skaegg.discordbot.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trivia_button_clicks")
public class TriviaButtonClicksEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "user_id")
    String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    TriviaQuestionsEntity question;

    @Column(name = "date_time_clicked")
    LocalDateTime dateTimeClicked;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TriviaQuestionsEntity getQuestion() {
        return question;
    }

    public void setQuestion(TriviaQuestionsEntity question) {
        this.question = question;
    }

    public LocalDateTime getDateTimeClicked() {
        return dateTimeClicked;
    }

    public void setDateTimeClicked(LocalDateTime dateTimeClicked) {
        this.dateTimeClicked = dateTimeClicked;
    }
}
