package se.skaegg.discordbot.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.client.HttpClient;
import se.skaegg.discordbot.dto.TheTriviaApiResults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TheTriviaApiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenTriviaClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    String url;
    String queryParams;

    public TheTriviaApiClient(String url, String numberOfQuestions) {
        this.url = url;
        this.queryParams = numberOfQuestions;
    }

    public TheTriviaApiResults process() {

        url = url + "?" + queryParams;

        final String response = HttpClient.create()
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        List<TheTriviaApiResults> jsonList = new ArrayList<>();
        TheTriviaApiResults questions = new TheTriviaApiResults();

        try {
            jsonList = MAPPER.readValue(response, new TypeReference<>() {
            });
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error(String.format("Couldn't map the json string to Trivia DTO %n%s %n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }

        return jsonList.get(0);
    }
}
