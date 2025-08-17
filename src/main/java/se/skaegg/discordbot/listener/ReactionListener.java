package se.skaegg.discordbot.listener;

import org.springframework.stereotype.Component;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handler.EmojiStats;

@Component
public class ReactionListener {

    EmojiStats emojiStats;

    public ReactionListener(GatewayDiscordClient client,
                            EmojiStats emojiStats) {
        this.emojiStats = emojiStats;
        client.on(ReactionAddEvent.class, this::handle).subscribe();
    }


    private Mono<Void> handle(ReactionAddEvent event) {
        String emojiId = event.getEmoji().asEmojiData().id().isPresent() ? event.getEmoji().asEmojiData().id().get().asString() : null;
        String channelId = event.getChannelId().asString();
        String userId = event.getUserId().asString();

        event.getEmoji().asEmojiData().name().ifPresent(name -> {
            if (name.matches("\\w+")) {
                emojiStats.saveEmojiUsage(name, channelId, userId, EmojiStats.emojiUseType.REACTION, emojiId);
            }
        });
        return Mono.empty();
    }
}
