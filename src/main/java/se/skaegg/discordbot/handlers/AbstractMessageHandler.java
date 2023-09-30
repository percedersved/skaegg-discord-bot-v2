package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.rest.http.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

/*
 * Holds helper methods that helps with creation of
 * messages to Discord
 */
public class AbstractMessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageHandler.class);

    /**
     * Takes a {@link discord4j.core.event.domain.interaction.DeferrableInteractionEvent} and call deferReply on it.
     * There is a 3-second time limit to reply to a message but if the bot needs more time
     * this can be used to give us more time. After the deferReply the time limit is 15 minutes.
     * This will also trigger the bot to show "thinking..." in the GUI.
     *
     * @param event         The ChatInputIntecationEvent to deferReply on
     * @param ephemeral     If this should be ephemeral. If the response later on should
     *                      be ephemeral the deferred reply also must be ephemeral
     */
    protected void deferEventReply(DeferrableInteractionEvent event, boolean ephemeral) {
        event.deferReply()
                .withEphemeral(ephemeral)
                .retry(3)
                .onErrorResume(e -> {
                    if (e instanceof ClientException) {
                        LOG.error("Discord4j ClientException: \n{}", e.getMessage());
                    } else if (e instanceof PrematureCloseException) {
                        LOG.error("Netty PrematureCloseException, something closed the connection: \n{}", e.getMessage());
                    } else {
                        LOG.error("An error occurred but was not ClientException or PrematureCloseException\n{}", e.getMessage());
                    }
                    return Mono.empty();
                }).subscribe();
    }
}
