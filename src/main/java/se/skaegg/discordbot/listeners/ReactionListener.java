package se.skaegg.discordbot.listeners;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.handlers.EmojiStats;

import java.util.Optional;

@Component
public class ReactionListener {

    @Autowired
    GatewayDiscordClient client;
    @Autowired
    EmojiStats emojiStats;

    public ReactionListener(GatewayDiscordClient client) {
        client.on(ReactionAddEvent.class, this::handle).subscribe();
    }


    private Mono<Void> handle(ReactionAddEvent event) {
        Optional<String> emojiName = event.getEmoji().asEmojiData().name();
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
