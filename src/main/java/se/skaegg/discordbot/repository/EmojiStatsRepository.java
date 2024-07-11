package se.skaegg.discordbot.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.EmojiStats;
import se.skaegg.discordbot.dto.EmojiStatsCount;
import se.skaegg.discordbot.dto.EmojiStatsCountPerDayRaw;

@Repository
public interface EmojiStatsRepository extends JpaRepository<EmojiStats, Integer> {

    @Query("SELECT new se.skaegg.discordbot.dto.EmojiStatsCount(e.name, COUNT(e.id)) "
            + "FROM EmojiStats AS e GROUP BY e.name ORDER BY COUNT(e.id) DESC")
    List<EmojiStatsCount> countTotalPerEmoji();

    List<EmojiStats> findAllByDateBetween(LocalDate from, LocalDate to);

    @Query("SELECT NEW se.skaegg.discordbot.dto.EmojiStatsCountPerDayRaw(" +
            "e.date, " +
            "e.name, " +
            "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) as msgCount, " +
            "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) as reactCount, " +
            "e.emojiId" +
            ") " +
            "FROM EmojiStats e " +
            "GROUP BY e.date, e.name " +
            "HAVING " +
            "SUM(CASE WHEN e.useType = 'MESSAGE' THEN 1 ELSE 0 END) > 0 OR " +
            "SUM(CASE WHEN e.useType = 'REACTION' THEN 1 ELSE 0 END) > 0" +
            "ORDER BY e.date")
    List<EmojiStatsCountPerDayRaw> countTotalPerDay();
}
