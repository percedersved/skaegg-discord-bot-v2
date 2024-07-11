package se.skaegg.discordbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

	Member findByMemberIdAndServerId(String memberId, String serverId);

	List<Member> findAllByServerId(String serverId);
}
