package se.skaegg.discordbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.skaegg.discordbot.entity.Member;
import se.skaegg.discordbot.entity.VcSubscriptionUser;

import java.util.List;

@Repository
public interface VcSubscriptionUsersRepository extends JpaRepository<VcSubscriptionUser, Integer> {

    List<VcSubscriptionUser> findAllByServerId(String serverId);

    VcSubscriptionUser findByMemberAndServerId(Member member, String serverId);
}
