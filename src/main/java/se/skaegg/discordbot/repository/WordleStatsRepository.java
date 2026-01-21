package se.skaegg.discordbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.skaegg.discordbot.dto.WordleStatsSummary;
import se.skaegg.discordbot.dto.WordleStatsTriesCount;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.WordleStats;

@Repository
public interface WordleStatsRepository extends JpaRepository<WordleStats, Integer> {

    long countByMember(Member member);

    long countByMemberAndTries(Member member, Integer tries);

    @Query("""
        select new se.skaegg.discordbot.dto.WordleStatsTriesCount(
            ws.tries,
            count(ws)
        )
        from WordleStats ws
        where ws.member.memberId = :memberId
            and ws.tries between 0 and 6
        group by ws.tries
        order by ws.tries
    """)
    List<WordleStatsTriesCount> findTriesDistribution(@Param("memberId") Long memberId);

    @Query("""
        select new se.skaegg.discordbot.dto.WordleStatsSummary(
            ws.member,
            count(ws),
            avg(case when ws.tries = 0 then 7 else ws.tries end)
        )
        from WordleStats ws
        group by ws.member.memberId
    """)
    List<WordleStatsSummary> findMemberStatsSummary();
}
