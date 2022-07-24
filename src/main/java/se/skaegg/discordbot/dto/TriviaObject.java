package se.skaegg.discordbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TriviaObject {

    @JsonProperty("response_code")
    int responseCode;
    @JsonProperty("results")
    List<TriviaResults> results;


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public List<TriviaResults> getResults() {
        return results;
    }

    public void setResults(List<TriviaResults> results) {
        this.results = results;
    }
}
