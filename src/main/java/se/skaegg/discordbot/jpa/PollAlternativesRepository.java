package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollAlternativesRepository extends JpaRepository<PollAlternativesEntity, Integer> {

    PollAlternativesEntity findById(int id);
    List<PollAlternativesEntity> findByPollId(PollsEntity pollId);
}
