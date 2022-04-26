package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.clients.RandomRestaurantClient;
import se.skaegg.discordbot.dto.Restaurant;

import java.util.List;
import java.util.Optional;

@Component
public class RestaurantRandom implements SlashCommand {


    @Value("${restaurant.api.token}")
    private String token;

    @Value("${restaurant.api.url}")
    private String restaurantUrl;

    @Override
    public String getName() {
        return "restaurang";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        event.deferReply().subscribe();
        return getRestaurant(event).then();
    }

    private Mono<Message> getRestaurant(ChatInputInteractionEvent event) {

        Optional<String> searchWordOpt = event.getOption("plats")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);

        String whiteSpace = "\u200B";

        List<Restaurant> restaurantList;
        if (searchWordOpt.isEmpty()) {
            restaurantList = new RandomRestaurantClient(token, restaurantUrl).process();

        }
        else {
            restaurantList = new RandomRestaurantClient(token, restaurantUrl).process(searchWordOpt.get());
        }

        Restaurant restaurant = restaurantList.get(0);

        String leftColumnEmbed =
                restaurant.getRating() + "\n" +
                restaurant.getOpeningHours() + "\n" +
                restaurant.getPricing();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        leftColumnEmbed = leftColumnEmbed.isBlank() ? whiteSpace : leftColumnEmbed;

        String rightColumnEmbed =
                restaurant.getWebsite() + "\n" +
                restaurant.getAddress() + "\n" +
                restaurant.getPhone();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        rightColumnEmbed = rightColumnEmbed.isBlank() ? whiteSpace : rightColumnEmbed;

        String footerEmbed =
                "\u200B\n" +
                restaurant.getReviewText() + "\n" +
                restaurant.getReviewByline();


        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title(restaurant.getName())
                .url(restaurant.getUrl())
                .addField("Information", leftColumnEmbed, true)
                .addField("Kontakt", rightColumnEmbed, true)
                .image(restaurant.getPhoto())
                .footer(footerEmbed, "")
                .build();


        return event.createFollowup()
                .withEmbeds(embed);
    }
}
