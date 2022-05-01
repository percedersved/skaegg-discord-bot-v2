package se.skaegg.discordbot.handlers;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.clients.RandomRestaurantClient;
import se.skaegg.discordbot.dto.Restaurant;

import java.util.List;
import java.util.Optional;

@Component
public class RestaurantDuel implements SlashCommand {

    @Value("${restaurant.api.token}")
    private String token;

    @Value("${restaurant.api.url}")
    private String restaurantUrl;

    @Value("${restaurant.duel.replytext}")
    private String replytext;


    @Override
    public String getName() {
        return "restaurangduell";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        event.deferReply().subscribe();
        return getRestaurant(event)
                .flatMap(msg -> msg.addReaction(ReactionEmoji.unicode("🔴"))
                        .then(msg.addReaction(ReactionEmoji.unicode("\uD83D\uDD35"))));
    }

    private Mono<Message> getRestaurant(ChatInputInteractionEvent event) {

        Optional<String> searchWordOpt = event.getOption("plats")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);

        String searchWordOptVal = searchWordOpt.orElse("Norrtälje");

        String whiteSpace = "\u200B";

        List<Restaurant> restaurantList = new RandomRestaurantClient(token, restaurantUrl).process(searchWordOptVal, 2);


        Restaurant restaurantOne = restaurantList.get(0);
        Restaurant restaurantTwo = restaurantList.get(1);

        String leftColumnEmbedROne =
                restaurantOne.getRating() + "\n" +
                restaurantOne.getOpeningHours() + "\n" +
                restaurantOne.getPricing();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        leftColumnEmbedROne = leftColumnEmbedROne.isBlank() ? whiteSpace : leftColumnEmbedROne;

        String rightColumnEmbedROne =
                restaurantOne.getWebsite() + "\n" +
                restaurantOne.getAddress() + "\n" +
                restaurantOne.getPhone();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        rightColumnEmbedROne = rightColumnEmbedROne.isBlank() ? whiteSpace : rightColumnEmbedROne;


        String footerEmbedROne =
                "\u200B\n" +
                restaurantOne.getReviewText() + "\n" +
                restaurantOne.getReviewByline();

        EmbedCreateSpec embedROne = EmbedCreateSpec.builder()
                .color(Color.of(221, 46, 68))
                .title(":red_circle: " + restaurantOne.getName())
                .url(restaurantOne.getUrl())
                .addField("Information", leftColumnEmbedROne, true)
                .addField("Kontakt", rightColumnEmbedROne, true)
                .footer(footerEmbedROne, "")
                .build();


        String leftColumnEmbedRTwo =
                restaurantTwo.getRating() + "\n" +
                restaurantTwo.getOpeningHours() + "\n" +
                restaurantTwo.getPricing();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        leftColumnEmbedRTwo = leftColumnEmbedRTwo.isBlank() ? whiteSpace : leftColumnEmbedRTwo;

        String rightColumnEmbedRTwo =
                restaurantTwo.getWebsite() + "\n" +
                restaurantTwo.getAddress() + "\n" +
                restaurantTwo.getPhone();
        // Check the column only contains whitespace, then add the special whitespace char to avoid crash
        rightColumnEmbedRTwo = rightColumnEmbedRTwo.isBlank() ? whiteSpace : rightColumnEmbedRTwo;

        String footerEmbedRTwo =
                "\u200B\n" +
                restaurantTwo.getReviewText() + "\n" +
                restaurantTwo.getReviewByline();

        EmbedCreateSpec embedRTwo = EmbedCreateSpec.builder()
                .color(Color.of(85, 172, 238))
                .title(":blue_circle: " + restaurantTwo.getName())
                .url(restaurantTwo.getUrl())
                .addField("Information", leftColumnEmbedRTwo, true)
                .addField("Kontakt", rightColumnEmbedRTwo, true)
                .footer(footerEmbedRTwo, "")
                .build();

        List<EmbedCreateSpec> embeds = List.of(embedROne, embedRTwo);
        Possible<Optional<List<EmbedCreateSpec>>> embeds2 = Possible.of(Optional.of(List.of(embedROne, embedRTwo)));

        // Capitalize first letter to use in reply
        String searchWordOptValCapitalized = searchWordOptVal.substring(0, 1).toUpperCase() + searchWordOptVal.substring(1);

        return event.createFollowup()
                .withContent(String.format(replytext, searchWordOptValCapitalized))
                .withEmbeds(embeds);
    }
}
