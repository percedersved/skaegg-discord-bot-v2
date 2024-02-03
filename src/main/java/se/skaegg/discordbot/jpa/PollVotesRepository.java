package se.skaegg.discordbot.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollVotesRepository extends JpaRepository<PollVotesEntity, Integer> {

    PollVotesEntity findByAlternativeIdAndUserId(PollAlternativesEntity pollAlternativesEntity, String userId);

    @Query(value = """            
            SELECT new se.skaegg.discordbot.jpa.PollVotesPerPollId(A.value as alternativeName, count(V.id) as voteCount)
            FROM PollVotesEntity V
            RIGHT JOIN PollAlternativesEntity A
            ON V.alternativeId.id = A.id
            WHERE A.pollId = ?1
            GROUP BY A.id
            """)
    List<PollVotesPerPollId> countVotesPerPollId(PollsEntity pollId);
}
