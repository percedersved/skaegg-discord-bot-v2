package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandStatisticsRepository extends JpaRepository<CommandStatisticsEntity, Integer> {

    @Query("SELECT new se.skaegg.discordbot.jpa.CommandStatisticsCountUsers(c.calledByUserId, COUNT(c.id)) "
            + "FROM CommandStatisticsEntity AS c WHERE c.commandName NOT IN ?1 GROUP BY c.calledByUserId ORDER BY COUNT(c.id) DESC")
    List<CommandStatisticsCountUsers> countTotalIdsByUserId(List<String> excluedParameters);

    @Query("SELECT new se.skaegg.discordbot.jpa.CommandStatisticsCountCommands(c.commandName, COUNT(c.id)) "
        + "FROM CommandStatisticsEntity AS c WHERE c.commandName NOT IN ?1 GROUP BY c.commandName ORDER BY COUNT(c.id) DESC")
    List<CommandStatisticsCountCommands> countTotalIdsByCommandName(List<String> excludedParameters);
}
