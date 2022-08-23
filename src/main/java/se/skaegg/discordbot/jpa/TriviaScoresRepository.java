package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TriviaScoresRepository extends JpaRepository<TriviaScoresEntity, Integer> {

    TriviaScoresEntity findByUserIdAndAnswerDate(String userId, LocalDate answerDate);

    TriviaScoresEntity findByUserIdAndQuestion(String userId, TriviaQuestionsEntity question);

    @Query("SELECT new se.skaegg.discordbot.jpa.TriviaScoresCountPoints(COUNT(s.id), s.userId) " +
            "FROM TriviaScoresEntity AS s " +
            "INNER JOIN TriviaQuestionsEntity AS q  " +
            "ON s.question = q.id " +
            "WHERE s.correctAnswer = 1 AND q.questionDate BETWEEN ?1 AND ?2 " +
            "GROUP BY s.userId " +
            "ORDER BY COUNT(s.id) DESC")
    List<TriviaScoresCountPoints> countTotalIdsByAnswerAndDates(LocalDate fromDate, LocalDate toDate);


    @Query("SELECT new se.skaegg.discordbot.jpa.TriviaPercentageForDateEntity(Q.questionDate, Q.question, (CAST(SUM(S.correctAnswer) AS float) / CAST(COUNT(*) AS float)) * 100.0)\n" +
            "FROM TriviaScoresEntity S\n" +
            "LEFT OUTER JOIN TriviaQuestionsEntity Q ON Q.id = S.question\n" +
            "WHERE Q.questionDate = ?1\n" +
            "GROUP BY Q.id")
    TriviaPercentageForDateEntity percentageCorrectByDate2(LocalDate date);
}
