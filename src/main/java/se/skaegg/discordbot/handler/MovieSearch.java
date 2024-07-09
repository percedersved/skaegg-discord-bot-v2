package se.skaegg.discordbot.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.client.OmdbClient;
import se.skaegg.discordbot.dto.OmdbMovie;
import se.skaegg.discordbot.dto.OmdbSearchObject;
import se.skaegg.discordbot.dto.OmdbSearchResult;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieSearch implements SlashCommand{

    @Autowired
    GatewayDiscordClient client;

    @Value("${omdb.api.token}")
    String apiToken;

    @Override
    public String getName() {
        return "film";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        event.deferReply().subscribe();

        return searchMovie(event).then();
    }


    private Mono<Message> searchMovie(ChatInputInteractionEvent event) {

        // For reference it could be done like this: event.getOption("title").get().getValue().get().asString();
        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String searchWord = event.getOption("titel")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get();

        OmdbSearchObject searchObject;
        try {
            searchObject = new OmdbClient(apiToken).searchMovie(searchWord);
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            return event.createFollowup()
                    .withContent("Nu gick något fel. Testa igen vettja!");
        }

        // SearchObject holds a list of searchresults. If property response is false, something went wrong
        List<OmdbSearchResult> searchResults;
        if (!searchObject.getResponse().equals("False")) {
            searchResults = searchObject.getSearch();
        }
        else {
            return event.createFollowup()
                    .withEphemeral(true)
                    .withContent(searchObject.getError());
        }

        List<SelectMenu.Option> titles = new ArrayList<>();
        for (OmdbSearchResult result : searchResults) {
            titles.add(SelectMenu.Option.of(result.getTitle(), result.getImdbID())
                    .withDescription(result.getType() + " - " + result.getYear()));
        }

        SelectMenu selectMenu = SelectMenu.of("movies", titles)
                .withMaxValues(1)
                .withMinValues(1)
                .withPlaceholder("Välj film");

        return event.editReply()
            .withComponents(ActionRow.of(selectMenu));
    }


    public Mono<Message> getMovie(SelectMenuInteractionEvent event, String apiToken) {
        String imdbId = event.getValues().get(0);

        OmdbMovie movie;
        try {
            movie = new OmdbClient(apiToken).getMovie(imdbId);
        }
        catch (JsonProcessingException e) {
            return event.createFollowup()
                    .withContent("Nu gick något fel. Testa igen vettja!");
        }

        String successfull = movie.getResponse();

        EmbedCreateSpec embed = EmbedCreateSpec.builder().build(); // Needs to be instantiated outside of the if statement below

        if (!successfull.equals("False")) {
            String title = movie.getTitle();
            String description = movie.getPlot();
            String actors = movie.getActors();
            String genre = movie.getGenre();
            String released = movie.getReleased();
            String imdbRating = movie.getImdbRating();
            String awards = movie.getAwards();
            String imageUrl = movie.getPoster();
            String totalSeasons = movie.getTotalSeasons() == null ? "" : "\n** *Antal säsonger:** " + movie.getTotalSeasons();

            String otherInfo = "** * Genre:** " + genre +
                    "\n** * Skådisar:** " + actors +
                    "\n** * Släpptes:** " + released +
                    "\n** * IMDB rating:** " + imdbRating +
                    "\n** * Priser:** " + awards +
                    totalSeasons;

            String imdbLink = "https://www.imdb.com/title/" + movie.getImdbID();


            embed = EmbedCreateSpec.builder()
                    .color(Color.of(90, 130, 180))
                    .title(title)
                    .image(imageUrl.equals("N/A") ? "https://freesvg.org/img/skotan-No-sign.png" : imageUrl)
                    .addField("Handling", description, true)
                    .addField("Övrigt", otherInfo, true)
                    .url(imdbLink)
                    .build();
        }

        if (successfull.equals("False")) {
            event.createFollowup()
                    .withContent("Din sökning gav ingen träff. No movie for you! <:koerdittjaeklaboegrace:814187249288872016>")
                    .subscribe();
        }
        else {
            event.createFollowup()
                    .withEmbeds(embed)
                    .subscribe();
        }
        return event.getInteraction()
                .getMessage()
                .get()
                .edit()
                .withComponents(ActionRow.of(SelectMenu.of("disabled", SelectMenu.Option.ofDefault(movie.getTitle(), "disabled"))
                        .disabled()));


    }
}
