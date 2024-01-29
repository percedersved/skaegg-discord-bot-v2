package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmojiStatsRepository extends JpaRepository<EmojiStatsEntity, Integer> {

    @Query("SELECT new se.skaegg.discordbot.jpa.EmojiStatsCount(e.name, COUNT(e.id)) "
            + "FROM EmojiStatsEntity AS e GROUP BY e.name ORDER BY COUNT(e.id) DESC")
    List<EmojiStatsCount> countTotalPerEmoji();

    List<EmojiStatsEntity> findAllByDateBetween(LocalDate from, LocalDate to);

//    @Query("SELECT new se.skaegg.discordbot.jpa.EmojiStatsCountPerDay(date, name, COUNT(*)) "
//            + "FROM EmojiStatsEntity WHERE date BETWEEN ?1 AND ?2 GROUP BY date, name ORDER BY date")
//    List<EmojiStatsCountPerDay> countTotalPerDayDateFilter(LocalDate from, LocalDate to);

    @Query("SELECT new se.skaegg.discordbot.jpa.EmojiStatsCountPerDay(e.date, e.name, e.useType, COUNT(*)) "
            + "FROM EmojiStatsEntity AS e GROUP BY e.date, e.name, e.useType ORDER BY e.date")
    List<EmojiStatsCountPerDay> countTotalPerDay();


    @Query("SELECT NEW se.skaegg.discordbot.jpa.EmojiStatsCountPerDay2(" +
                  "e.date, " +
                  "e.name, " +
                  "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) as msgCount, " +
                  "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) as reactCount" +
                  ") " +
                  "FROM EmojiStatsEntity e " +
                  "GROUP BY e.date, e.name " +
                  "HAVING " +
                  "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) > 0 OR " +
                  "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) > 0")
    public List<EmojiStatsCountPerDay2> countTotalPerDay2();
}
