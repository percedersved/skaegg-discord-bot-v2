package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollsRepository extends JpaRepository<PollsEntity, Integer> {

    PollsEntity findById(int id);
    List<PollsEntity> findByNameContainingAndProcessed(String name, Boolean processed);
    List<PollsEntity> findByProcessed(Boolean processed);
}
