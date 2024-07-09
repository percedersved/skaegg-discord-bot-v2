package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.PollAlternatives;
import se.skaegg.discordbot.entity.Polls;

import java.util.List;

@Repository
public interface PollAlternativesRepository extends JpaRepository<PollAlternatives, Integer> {

    PollAlternatives findById(int id);
    List<PollAlternatives> findByPollId(Polls pollId);
}
