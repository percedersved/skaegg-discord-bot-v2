package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.PollAlternatives;
import se.skaegg.discordbot.entity.PollVotes;
import se.skaegg.discordbot.entity.Polls;
import se.skaegg.discordbot.dto.PollVotesPerPollId;

import java.util.List;

@Repository
public interface PollVotesRepository extends JpaRepository<PollVotes, Integer> {

    PollVotes findByAlternativeIdAndUserId(PollAlternatives pollAlternatives, String userId);

    @Query(value = """            
            SELECT new se.skaegg.discordbot.dto.PollVotesPerPollId(A.value as alternativeName, count(V.id) as voteCount)
            FROM PollVotes V
            RIGHT JOIN PollAlternatives A
            ON V.alternativeId.id = A.id
            WHERE A.pollId = ?1
            GROUP BY A.id
            """)
    List<PollVotesPerPollId> countVotesPerPollId(Polls pollId);
}
