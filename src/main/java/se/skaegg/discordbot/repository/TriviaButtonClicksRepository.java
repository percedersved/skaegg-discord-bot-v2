package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.TriviaButtonClicks;
import se.skaegg.discordbot.entity.TriviaQuestions;

@Repository
public interface TriviaButtonClicksRepository extends JpaRepository<TriviaButtonClicks, Integer> {

    TriviaButtonClicks findByUserIdAndQuestion(String userId, TriviaQuestions question);
}
