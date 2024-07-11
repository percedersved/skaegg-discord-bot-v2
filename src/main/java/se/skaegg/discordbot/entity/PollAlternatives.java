package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "poll_alternatives")
public class PollAlternatives {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "value", nullable = false)
    String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    Polls pollId;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Polls getPollId() {
        return pollId;
    }

    public void setPollId(Polls pollId) {
        this.pollId = pollId;
    }
}
