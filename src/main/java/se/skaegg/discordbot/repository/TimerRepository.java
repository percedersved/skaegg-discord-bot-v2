package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.Timer;

import java.util.List;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Integer> {
    List<Timer> findByKeyIgnoreCase(String timerKey);

    List<Timer> findByProcessed(Boolean processed);
}
