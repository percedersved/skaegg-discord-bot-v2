package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.CommandStatistics;
import se.skaegg.discordbot.dto.CommandStatisticsCountCommands;
import se.skaegg.discordbot.dto.CommandStatisticsCountUsers;

import java.util.List;

@Repository
public interface CommandStatisticsRepository extends JpaRepository<CommandStatistics, Integer> {

    @Query("SELECT new se.skaegg.discordbot.dto.CommandStatisticsCountUsers(c.calledByUserId, COUNT(c.id)) "
            + "FROM CommandStatistics AS c WHERE c.commandName NOT IN ?1 GROUP BY c.calledByUserId ORDER BY COUNT(c.id) DESC")
    List<CommandStatisticsCountUsers> countTotalIdsByUserId(List<String> excluedParameters);

    @Query("SELECT new se.skaegg.discordbot.dto.CommandStatisticsCountCommands(c.commandName, COUNT(c.id)) "
        + "FROM CommandStatistics AS c WHERE c.commandName NOT IN ?1 GROUP BY c.commandName ORDER BY COUNT(c.id) DESC")
    List<CommandStatisticsCountCommands> countTotalIdsByCommandName(List<String> excludedParameters);
}
