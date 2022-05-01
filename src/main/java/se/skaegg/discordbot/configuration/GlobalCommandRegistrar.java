package se.skaegg.discordbot.configuration;

import discord4j.common.JacksonResources;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GlobalCommandRegistrar implements ApplicationRunner {

    @Value("#{'${serverIds}'.split(',')}")
    List<String> serverIds;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final RestClient client;

    //Use the rest client provided by our Bean
    public GlobalCommandRegistrar(RestClient client) {
        this.client = client;
    }

    //This method will run only once on each start up and is automatically called with Spring so blocking is okay.
    @Override
    public void run(ApplicationArguments args) throws IOException {
        //Create an ObjectMapper that supported Discord4J classes
        final JacksonResources d4jMapper = JacksonResources.create();

        // Convenience variables for the sake of easier to read code below.
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        final ApplicationService applicationService = client.getApplicationService();
        final long applicationId = client.getApplicationId().block();

        //Get our commands json from resources as command data
        List<ApplicationCommandRequest> commands = new ArrayList<>();
        for (Resource resource : matcher.getResources("commands/*.json")) {
            ApplicationCommandRequest request = d4jMapper.getObjectMapper()
                    .readValue(resource.getInputStream(), ApplicationCommandRequest.class);

            commands.add(request);
        }

        /* Bulk overwrite commands. This is now idempotent, so it is safe to use this even when only 1 command
        is changed/added/removed
        */
        // Since the bot just lives on a few servers we're using guild commands instead of global. Go through list from application.properties
        // of serverIds and register the commands to those servers
        for (String serverId : serverIds) {
            long sId = Long.parseLong(serverId);
            applicationService.bulkOverwriteGuildApplicationCommand(applicationId, sId, commands)
                    .doOnNext(ignore -> LOGGER.debug("Successfully registered Guild Command"))
                    .doOnError(e -> LOGGER.error("Failed to register guild commands", e))
                    .subscribe();
        }



//        // Delete global command
//        // Get the commands from discord as a Map
//        Map<String, ApplicationCommandData> discordCommands = client.getApplicationService()
//                .getGlobalApplicationCommands(applicationId)
//                .collectMap(ApplicationCommandData::name)
//                .block();
//
//        // Get id of command
//        long commandId = Long.parseLong(discordCommands.get("bolagetopen").id());
//
//        // Delete it
//        client.getApplicationService()
//                .deleteGlobalApplicationCommand(applicationId, commandId)
//                .subscribe();
    }
}

