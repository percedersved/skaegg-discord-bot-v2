package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface SlashCommand {
    String getName();

    Mono<Void> handle(ChatInputInteractionEvent event);
}
