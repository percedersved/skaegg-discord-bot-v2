package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TriviaScoresRepository extends JpaRepository<TriviaScoresEntity, Integer> {

    TriviaScoresEntity findByUserIdAndQuestion(String userId, TriviaQuestionsEntity question);

    @Query(value = "SELECT new se.skaegg.discordbot.jpa.TriviaScoresCountPoints(COUNT(s.id), s.userId) " +
            "FROM TriviaScoresEntity AS s " +
            "INNER JOIN TriviaQuestionsEntity AS q  " +
            "ON s.question.id = q.id " +
            "WHERE s.correctAnswer = true AND q.questionDate BETWEEN ?1 AND ?2 " +
            "GROUP BY s.userId " +
            "ORDER BY COUNT(s.id) DESC")
    List<TriviaScoresCountPoints> countTotalIdsByAnswerAndDates(LocalDate fromDate, LocalDate toDate);


@Query("SELECT new se.skaegg.discordbot.jpa.TriviaPercentageForDateEntity(Q.questionDate, Q.question, ((CAST(SUM(S.correctAnswer) AS DOUBLE) / CAST(COUNT(*) AS float)) * 100.0)) " +
        "FROM TriviaScoresEntity S " +
        "LEFT OUTER JOIN TriviaQuestionsEntity Q ON Q.id = S.question.id " +
        "WHERE Q.questionDate = ?1 " +
        "GROUP BY Q.id")
    TriviaPercentageForDateEntity percentageCorrectByDate2(LocalDate date);


    @Query(value = "SELECT new se.skaegg.discordbot.jpa.TriviaAnswersPerUserMonth" +
            "(" +
            "   S.userId as userId, count(S.id) as points, CAST" +
            "   (" +
            "       (" +
            "           (" +
            "               SELECT CAST(count(S2.id) AS float) " +
            "               FROM TriviaScoresEntity AS S2 " +
            "               JOIN TriviaQuestionsEntity AS Q2 " +
            "               ON S2.question.id = Q2.id " +
            "               WHERE Q2.questionDate BETWEEN ?1 AND ?2 " +
            "               AND S2.userId = S.userId group by S2.userId" +
            "           ) * 100" +
            "       ) " +
            "       / " +
            "       (" +
            "           SELECT CAST(count(*) AS float) " +
            "           FROM TriviaQuestionsEntity where questionDate between ?1 AND ?2" +
            "       ) " +
            "   AS float) AS percent " +
            ") " +
            "FROM TriviaScoresEntity AS S " +
            "JOIN TriviaQuestionsEntity AS Q " +
            "ON S.question.id = Q.id " +
            "WHERE Q.questionDate between ?1 AND ?2 " +
            "AND S.correctAnswer = true " +
            "GROUP BY userId " +
            "ORDER BY count(S.id) DESC")
    List<TriviaAnswersPerUserMonth> answersPerUserAndMonth(LocalDate start, LocalDate end);

}
