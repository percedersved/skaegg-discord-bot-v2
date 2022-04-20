package se.skaegg.discordbot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
public class OmdbSearchObject {

    List<OmdbSearchResult> search;
    String totalResults;
    String response;
    String error;

    public List<OmdbSearchResult> getSearch() {
        return search;
    }

    public void setSearch(List<OmdbSearchResult> search) {
        this.search = search;
    }

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String totalResults) {
        this.totalResults = totalResults;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getError() {
        return "Oops det där gick inte: " + error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
