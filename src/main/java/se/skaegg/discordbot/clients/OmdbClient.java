package se.skaegg.discordbot.clients;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.netty.http.client.HttpClient;
import se.skaegg.discordbot.dto.OmdbMovie;
import se.skaegg.discordbot.dto.OmdbSearchObject;
import se.skaegg.discordbot.dto.OmdbSearchResult;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

public class OmdbClient {

    String apiToken;

    public OmdbClient(String apiToken) {
        this.apiToken = apiToken;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OmdbMovie getMovie(String imdbId) throws JsonProcessingException {

        String queryParams = apiToken + "&i=" + URLEncoder.encode(imdbId, Charset.defaultCharset());

        String response = HttpClient.create()
                .get()
                .uri("http://www.omdbapi.com/?plot=short&apikey=" + queryParams)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        // Returns the OmdbMovie Object after it has been mapped by jackson
        return MAPPER.readValue(response, new TypeReference<>() { });
    }

    public OmdbSearchObject searchMovie(String searchWord) throws JsonProcessingException {

        String queryParams = apiToken + "&s=" + URLEncoder.encode(searchWord, Charset.defaultCharset());

        String response = HttpClient.create()
                .get()
                .uri("http://www.omdbapi.com/?apikey=" + queryParams)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        // return the list of movies
        return MAPPER.readValue(response, new TypeReference<>() { });
    }
}

