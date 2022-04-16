package se.skaegg.discordbot.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.netty.http.client.HttpClient;
import se.skaegg.discordbot.dto.OmdbMovie;

import java.net.URLEncoder;
import java.nio.charset.Charset;

public class OmdbClient {

    String apiToken;

    public OmdbClient(String apiToken) {
        this.apiToken = apiToken;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OmdbMovie process(String searchWord) throws JsonProcessingException {

        String queryParams = apiToken + "&t=" + URLEncoder.encode(searchWord, Charset.defaultCharset());

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
}

