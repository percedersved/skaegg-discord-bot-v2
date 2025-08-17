package se.skaegg.discordbot.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import se.skaegg.discordbot.entity.TriviaButtonClicks;
import se.skaegg.discordbot.entity.TriviaQuestions;

@Repository
public interface TriviaButtonClicksRepository extends JpaRepository<TriviaButtonClicks, Integer> {

    TriviaButtonClicks findByUserIdAndQuestion(String userId, TriviaQuestions question);

    List<TriviaButtonClicks> findByQuestion(TriviaQuestions question);

    @Query("SELECT tbc.question.questionDate FROM TriviaButtonClicks tbc WHERE tbc.id = :id")
    LocalDate findQuestionDateById(Integer id);
}
