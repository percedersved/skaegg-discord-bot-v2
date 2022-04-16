package se.skaegg.discordbot.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.clients.OmdbClient;
import se.skaegg.discordbot.dto.OmdbMovie;

@Component
public class MovieSearch implements SlashCommand{

    @Value("${omdb.api.token}")
    String apiToken;

    @Override
    public String getName() {
        return "film";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        event.deferReply().subscribe();
        return getMovie(event).then();
    }


    private Mono<Message> getMovie(ChatInputInteractionEvent event) {
        // For reference it could be done like this: event.getOption("title").get().getValue().get().asString();
        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String searchWord = event.getOption("title")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .get();

        OmdbMovie movie;
        try {
            movie = new OmdbClient(apiToken).process(searchWord);
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
                    .title(title)
                    .image(imageUrl.equals("N/A") ? "empty" : imageUrl)
                    .addField("Handling", description, true)
                    .addField("Övrigt", otherInfo, true)
                    .url(imdbLink)
                    .build();
        }

        if (successfull.equals("False")) {
            return event.createFollowup()
                    .withContent("Din sökning gav ingen träff. No movie for you! <:koerdittjaeklaboegrace:814187249288872016>");
        }
        else {
            return event.createFollowup()
                    .withEmbeds(embed);
        }
    }
}
