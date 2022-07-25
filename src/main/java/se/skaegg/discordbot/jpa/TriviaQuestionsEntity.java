package se.skaegg.discordbot.jpa;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trivia_questions")
public class TriviaQuestionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "question")
    String question;

    @Column(name = "question_date")
    LocalDate questionDate;

    @Column(name = "correct_answer")
    String correctAnswer;

    @Column(name = "incorrect_answer_1")
    String incorrectAnswer1;

    @Column(name = "incorrect_answer_2")
    String incorrectAnswer2;

    @Column(name = "incorrect_answer_3")
    String incorrectAnswer3;

    @Column(name = "difficulty")
    String difficulty;

    @Column(name = "category")
    String category;

    @Column(name = "type")
    String type;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "question")
    Set<TriviaScoresEntity> scores;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public LocalDate getQuestionDate() {
        return questionDate;
    }

    public void setQuestionDate(LocalDate questionDate) {
        this.questionDate = questionDate;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getIncorrectAnswer1() {
        return incorrectAnswer1;
    }

    public void setIncorrectAnswer1(String incorrectAnswer1) {
        this.incorrectAnswer1 = incorrectAnswer1;
    }

    public String getIncorrectAnswer2() {
        return incorrectAnswer2;
    }

    public void setIncorrectAnswer2(String incorrectAnswer2) {
        this.incorrectAnswer2 = incorrectAnswer2;
    }

    public String getIncorrectAnswer3() {
        return incorrectAnswer3;
    }

    public void setIncorrectAnswer3(String incorrectAnswer3) {
        this.incorrectAnswer3 = incorrectAnswer3;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<TriviaScoresEntity> getScores() {
        return scores;
    }

    public void setScores(Set<TriviaScoresEntity> scores) {
        this.scores = scores;
    }

    public List<String> getIncorrectAnswers() {
        if (this.getType().equals("multiple")) {
            return List.of(this.incorrectAnswer1, this.incorrectAnswer2, this.incorrectAnswer3);
        }
        else {
            return List.of(this.incorrectAnswer1);
        }
    }
}
