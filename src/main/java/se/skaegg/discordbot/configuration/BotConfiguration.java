package se.skaegg.discordbot.configuration;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfiguration {

    private static final Logger log = LoggerFactory.getLogger( BotConfiguration.class );

    @Value("${token}")
    private String token;

    @Bean
    public <T extends Event> GatewayDiscordClient gatewayDiscordClient() {
        GatewayDiscordClient client = null;

        try {
            client = DiscordClientBuilder.create(token)
                    .build()
                    .gateway()
                    .setEnabledIntents(IntentSet.all())
                    .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.listening("/kommandon")))
                    .login()
                    .block();

            log.info("Client created and logged in. Continuing with listeners");
        }

        catch ( Exception exception ) {
            log.error( "Be sure to use a valid bot token! Exception: {}", exception.getMessage() );
        }

        return client;
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}