package se.skaegg.discordbot.service;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.repository.MemberRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void saveMember(List<Member> members, String guildId) {

        for (discord4j.core.object.entity.Member member : members) {
            Snowflake memberId = member.getId();
            String username = member.getUsername();
            String displayName = member.getDisplayName();
            String avatarUrl = member.getAvatarUrl();

            se.skaegg.discordbot.entity.Member existingMember = memberRepository.findByMemberIdAndServerId(memberId.asString(), guildId);
            se.skaegg.discordbot.entity.Member memberEntity;

            if (existingMember != null) {
                memberEntity = existingMember;
                setColorAndSaveMember(memberEntity, member.getColor());
            }
            else {
                memberEntity = new se.skaegg.discordbot.entity.Member();
                memberEntity.setMemberId(memberId.asString());
                memberEntity.setUsername(username);
                memberEntity.setDisplayName(displayName);
                memberEntity.setAvatarUrl(avatarUrl);
                memberEntity.setServerId(guildId);
                setColorAndSaveMember(memberEntity, member.getColor());
            }
        }
    }

    @Override
    public List<se.skaegg.discordbot.entity.Member> getMember(String serverId) {
        if (serverId != null) {
            return memberRepository.findAllByServerId(serverId);
        }
        else {
            return memberRepository.findAll();
        }
    }

    @Override
    public se.skaegg.discordbot.entity.Member getMemberByIdAndServer(String memberId, String serverId) {
        return memberRepository.findByMemberIdAndServerId(memberId, serverId);
    }

    /**
     * Set color and save member entity in a separate thread
     * using ExecutorService. This is needed because .block()
     * is not supported in reactor-http-nio-3 thread that is used here
     *
     * @param member MemberEntity to save
     * @param colorMono Color Mono to save for the member
     */

    private void setColorAndSaveMember(se.skaegg.discordbot.entity.Member member, Mono<Color> colorMono) {

        executorService.submit(() -> {
            String colorHex = colorMono
                    .map(color -> String.format("#%06X", color.getRGB() & 0xFFFFFF)).block();
            member.setColor(colorHex);
            memberRepository.save(member); // Save member entity
        });
    }
}
