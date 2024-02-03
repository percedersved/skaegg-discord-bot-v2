package se.skaegg.discordbot.jpa;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmojiStatsRepository extends JpaRepository<EmojiStatsEntity, Integer> {

    @Query("SELECT new se.skaegg.discordbot.jpa.EmojiStatsCount(e.name, COUNT(e.id)) "
            + "FROM EmojiStatsEntity AS e GROUP BY e.name ORDER BY COUNT(e.id) DESC")
    List<EmojiStatsCount> countTotalPerEmoji();

    List<EmojiStatsEntity> findAllByDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT NEW se.skaegg.discordbot.jpa.EmojiStatsCountPerDayRaw(" +
            "e.date, " +
            "e.name, " +
            "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) as msgCount, " +
            "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) as reactCount, " +
            "e.emojiId" +
            ") " +
            "FROM EmojiStatsEntity e " +
            "GROUP BY e.date, e.name " +
            "HAVING " +
            "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) > 0 OR " +
            "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) > 0" +
            "ORDER BY e.date")
    List<EmojiStatsCountPerDayRaw> countTotalPerDay();
}
