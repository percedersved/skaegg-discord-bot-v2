package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TriviaQuestionsRepository extends JpaRepository<TriviaQuestionsEntity, Integer> {

    TriviaQuestionsEntity findByQuestionDate(LocalDate questionDate);

    TriviaQuestionsEntity findById(int id);
}
