package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "poll_votes")
public class PollVotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "user_id", nullable = false)
    String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    Polls pollId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternative_id", nullable = false)
    PollAlternatives alternativeId;


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

    public Polls getPollId() {
        return pollId;
    }

    public void setPollId(Polls pollId) {
        this.pollId = pollId;
    }

    public PollAlternatives getAlternativeId() {
        return alternativeId;
    }

    public void setAlternativeId(PollAlternatives alternativeId) {
        this.alternativeId = alternativeId;
    }
}
