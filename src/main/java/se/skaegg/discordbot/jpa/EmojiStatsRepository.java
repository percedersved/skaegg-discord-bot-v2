package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmojiStatsRepository extends JpaRepository<EmojiStatsEntity, Integer> {

    @Query("SELECT new se.skaegg.discordbot.jpa.EmojiStatsCount(e.name, COUNT(e.id)) "
            + "FROM EmojiStatsEntity AS e GROUP BY e.name ORDER BY COUNT(e.id) DESC")
    List<EmojiStatsCount> countTotalPerEmoji();
}
