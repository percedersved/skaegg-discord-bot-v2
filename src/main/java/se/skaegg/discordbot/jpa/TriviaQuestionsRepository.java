package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TriviaQuestionsRepository extends JpaRepository<TriviaQuestionsEntity, Integer> {

    TriviaQuestionsEntity findByQuestionDate(LocalDate questionDate);

    TriviaQuestionsEntity findById(int id);

    List<TriviaQuestionsEntity> findByQuestion(String question);

    @Query("""
            SELECT questionDate
                FROM TriviaQuestionsEntity
                GROUP BY questionDate
            """)
    List<LocalDate> findAllDistinctQuestionDates();

    @Query("""
            SELECT questionDate
                FROM TriviaQuestionsEntity
                WHERE questionDate BETWEEN ?1 AND ?2
                GROUP BY questionDate
            """)
    List<LocalDate> findAllDistinctQuestionDatesBetween(LocalDate fromDate, LocalDate toDate);
}
