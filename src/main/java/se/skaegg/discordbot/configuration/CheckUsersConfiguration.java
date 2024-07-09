package se.skaegg.discordbot.configuration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import se.skaegg.discordbot.service.MemberService;
import se.skaegg.discordbot.service.MemberServiceImpl;

import java.util.List;

@Configuration
@EnableScheduling
public class CheckUsersConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(CheckUsersConfiguration.class);
	private final MemberService memberService;
	private final GatewayDiscordClient client;
	private final List<String> serverIds;

	public CheckUsersConfiguration(GatewayDiscordClient client, @Value("${serverIds}") String serverIds,
								   MemberServiceImpl memberService) {
		this.client = client;
		this.serverIds = List.of(serverIds.split(","));
		this.memberService = memberService;
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
						memberService.saveMember(members, serverId);
					});
		}
	}


}