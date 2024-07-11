package se.skaegg.discordbot.configuration;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import se.skaegg.discordbot.handler.Poll;
import se.skaegg.discordbot.entity.Polls;
import se.skaegg.discordbot.repository.PollsRepository;

import java.util.List;

@Configuration
@EnableScheduling
public class CheckPollsConfiguration {

    @Autowired
    GatewayDiscordClient client;
    @Autowired
    Poll poll;
    PollsRepository pollsRepository;

    public CheckPollsConfiguration(PollsRepository pollsRepository) {
        this.pollsRepository = pollsRepository;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void SchedulePollCheck() {
        List<Polls> polls = pollsRepository.findByProcessed(false);

        polls.stream()
                .filter(Polls::isPassedOrToday)
                .forEach(pollEntity -> {
                    client.getChannelById(Snowflake.of(pollEntity.getChannelId()))
                            .ofType(MessageChannel.class)
                            .flatMap(channel -> channel.createMessage(poll.createPollChartEmbedSpec(
                                    pollEntity, String.format("Omröstning: %s är nu avslutad", pollEntity.getName()))))
                            .subscribe();
                    pollEntity.setProcessed(true);
                    pollsRepository.save(pollEntity);
                });
    }
}
