package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.TriviaQuestions;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TriviaQuestionsRepository extends JpaRepository<TriviaQuestions, Integer> {

    TriviaQuestions findByQuestionDate(LocalDate questionDate);

    TriviaQuestions findById(int id);

    List<TriviaQuestions> findByQuestion(String question);

    @Query("""
            SELECT questionDate
                FROM TriviaQuestions
                GROUP BY questionDate
            """)
    List<LocalDate> findAllDistinctQuestionDates();

    @Query("""
            SELECT questionDate
                FROM TriviaQuestions
                WHERE questionDate BETWEEN ?1 AND ?2
                GROUP BY questionDate
            """)
    List<LocalDate> findAllDistinctQuestionDatesBetween(LocalDate fromDate, LocalDate toDate);
}
