package se.skaegg.discordbot.service;


import se.skaegg.discordbot.entity.Member;

import java.util.List;

public interface MemberService {

    void saveMember(List<discord4j.core.object.entity.Member> members, String guildId);
    List<Member> getMember(String serverId);
    Member getMemberByIdAndServer(String memberId, String serverId);
}
