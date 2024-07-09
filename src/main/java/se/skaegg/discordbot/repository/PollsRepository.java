package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.Polls;

import java.util.List;

@Repository
public interface PollsRepository extends JpaRepository<Polls, Integer> {

    Polls findById(int id);
    List<Polls> findByNameContainingAndProcessed(String name, Boolean processed);
    List<Polls> findByProcessed(Boolean processed);
}
