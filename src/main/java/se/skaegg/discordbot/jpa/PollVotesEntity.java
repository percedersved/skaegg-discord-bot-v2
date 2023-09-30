package se.skaegg.discordbot.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "poll_votes")
public class PollVotesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    PollsEntity pollId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_id", nullable = false)
    PollAlternativesEntity alternativeId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PollsEntity getPollId() {
        return pollId;
    }

    public void setPollId(PollsEntity pollId) {
        this.pollId = pollId;
    }

    public PollAlternativesEntity getAlternativeId() {
        return alternativeId;
    }

    public void setAlternativeId(PollAlternativesEntity alternativeId) {
        this.alternativeId = alternativeId;
    }
}
