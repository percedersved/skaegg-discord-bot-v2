package se.skaegg.discordbot.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {

	MemberEntity findByMemberIdAndServerId(String memberId, String serverId);

	List<MemberEntity> findAllByServerId(String serverId);
}
