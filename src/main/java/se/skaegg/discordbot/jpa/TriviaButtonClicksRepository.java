package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriviaButtonClicksRepository extends JpaRepository<TriviaButtonClicksEntity, Integer> {

    TriviaButtonClicksEntity findByUserIdAndQuestion(String userId, TriviaQuestionsEntity question);
}
