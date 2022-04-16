package se.skaegg.discordbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OmdbRating {

    String source;
    String value;

    public String getSource() {
        return source;
    }

    @JsonProperty("Source")
    public void setSource(String source) {
        this.source = source;
    }

    public String getValue() {
        return value;
    }

    @JsonProperty("Value")
    public void setValue(String value) {
        this.value = value;
    }
}
