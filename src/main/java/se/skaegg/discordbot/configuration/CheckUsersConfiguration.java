package se.skaegg.discordbot.configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Color;
import jakarta.transaction.Transactional;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.jpa.MemberEntity;
import se.skaegg.discordbot.jpa.MemberRepository;

@Configuration
@EnableScheduling
public class CheckUsersConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(CheckUsersConfiguration.class);
	private final GatewayDiscordClient client;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final MemberRepository memberRepository;
	private final List<String> serverIds;

	public CheckUsersConfiguration(GatewayDiscordClient client, MemberRepository memberRepository,
	                               @Value("${serverIds}") String serverIds) {
		this.client = client;
		this.memberRepository = memberRepository;
		this.serverIds = List.of(serverIds.split(","));
	}

	@Scheduled(cron = "${getusers.cron.expression}")
	public void updateUsersFromServers() {

		for (String serverId : serverIds) {
			Snowflake guildId = Snowflake.of(serverId);
			client.getGuildById(guildId)
					.flatMapMany(Guild::getMembers)
					.collectList()
					.subscribe(members -> {
						LOG.debug("Saving members to DB. Total members on server {}: {}", serverId, members.size());
						saveOrUpdateMemberInDb(members, serverId);
					});
		}
	}

	@Transactional
	private void saveOrUpdateMemberInDb(List<Member> members, String guildId) {

		for (Member member : members) {
			Snowflake memberId = member.getId();
			String username = member.getUsername();
			String displayName = member.getDisplayName();
			String avatarUrl = member.getAvatarUrl();

			MemberEntity existingMember = memberRepository.findByMemberIdAndServerId(memberId.asString(), guildId);
			MemberEntity memberEntity;

			if (existingMember != null) {
				memberEntity = existingMember;
				setColorAndSaveMember(memberEntity, member.getColor());
			}
			else {
				memberEntity = new MemberEntity();
				memberEntity.setMemberId(memberId.asString());
				memberEntity.setUsername(username);
				memberEntity.setDisplayName(displayName);
				memberEntity.setAvatarUrl(avatarUrl);
				memberEntity.setServerId(guildId);
				setColorAndSaveMember(memberEntity, member.getColor());
			}
		}
	}

	/**
	 * Set color and save member entity in a separate thread
	 * using ExecutorService. This is needed because .block()
	 * is not supported in reactor-http-nio-3 thread that is used here
	 *
	 * @param memberEntity MemberEntity to save
	 * @param colorMono Color Mono to save for the member
	 */

	private void setColorAndSaveMember(MemberEntity memberEntity, Mono<Color> colorMono) {

		executorService.submit(() -> {
			String colorHex = colorMono
					.map(color -> String.format("#%06X", color.getRGB() & 0xFFFFFF)).block();
			memberEntity.setColor(colorHex);
			memberRepository.save(memberEntity); // Save member entity
		});
	}
}