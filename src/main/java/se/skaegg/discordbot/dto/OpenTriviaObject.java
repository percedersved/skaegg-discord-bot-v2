package se.skaegg.discordbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OpenTriviaObject {

    @JsonProperty("response_code")
    int responseCode;
    @JsonProperty("results")
    List<OpenTriviaResults> results;


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<OpenTriviaResults> getResults() {
        return results;
    }

    public void setResults(List<OpenTriviaResults> results) {
        this.results = results;
    }
}
