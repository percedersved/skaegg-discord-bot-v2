package se.skaegg.discordbot.jpa;

public class PollVotesPerPollId {
    String alternativeName;
    Long voteCount;

    public PollVotesPerPollId(String alternativeName, Long voteCount) {
        this.alternativeName = alternativeName;
        this.voteCount = voteCount;
    }

    public PollVotesPerPollId() {
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }
}
