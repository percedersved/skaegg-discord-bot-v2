package se.skaegg.discordbot.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.EmojiStats;

@Component
public class MessageListener {

    EmojiStats emojiStats;

    public MessageListener(GatewayDiscordClient client,
                           EmojiStats emojiStats) {
        this.emojiStats = emojiStats;
        client.on(MessageCreateEvent.class, this::handle).subscribe();
    }

    private Mono<Void> handle(MessageCreateEvent event) {
        String msgContent = event.getMessage().getData().content();
        if (msgContent.matches(".*<:(.+):.*>.*")) {
            MessageChannel channel = event.getMessage().getChannel().block();
            assert channel != null;
            String channelId = channel.getId().asString();
            String userId = event.getMessage().getAuthor().isPresent() ?
                    event.getMessage().getAuthor().get().getId().asString() : null;

            Pattern pattern = Pattern.compile("(<a?)?:(\\w+):(\\d+)>?");
            Matcher matcher = pattern.matcher(msgContent);
            while (matcher.find()) {
                    emojiStats.saveEmojiUsage(matcher.group(2), channelId, userId, EmojiStats.emojiUseType.MESSAGE, matcher.group(3));
                }
        }
        return Mono.empty();
    }
}