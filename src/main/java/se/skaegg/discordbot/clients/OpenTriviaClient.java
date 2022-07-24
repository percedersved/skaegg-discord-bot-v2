package se.skaegg.discordbot.clients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.client.HttpClient;
import se.skaegg.discordbot.dto.TriviaObject;

import java.util.Arrays;

public class OpenTriviaClient {

    private static final Logger log = LoggerFactory.getLogger(OpenTriviaClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    String url;
    String queryParams;

    public OpenTriviaClient(String url, String numberOfQuestions) {
        this.url = url;
        this.queryParams = numberOfQuestions;
    }

    public TriviaObject process() {

        url = url + "?" + queryParams;

        final String response = HttpClient.create()
                .get()
                .uri(url)
                .responseContent()
                .aggregate()
                .asString()
                .block();

        TriviaObject questions = new TriviaObject();

        try {
            questions = MAPPER.readValue(response, new TypeReference<>() {});
        }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error(String.format("Couldn't map the json string to Trivia DTO %n%s %n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }

        return questions;
    }



}
