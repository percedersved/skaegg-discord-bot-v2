package se.skaegg.discordbot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "polls")
public class Polls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    Integer id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "user_id", nullable = false)
    String userId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pollId")
    Set<PollAlternatives> pollAlternatives;

    @Column(name = "origin_channel_id")
    String channelId;

    @Column(name = "processed")
    Boolean processed;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Set<PollAlternatives> getPollAlternatives() {
        return pollAlternatives;
    }

    public void setPollAlternatives(Set<PollAlternatives> pollAlternatives) {
        this.pollAlternatives = pollAlternatives;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    public boolean isPassedOrToday() {
        return endDate.isBefore(LocalDate.now()) || endDate.isEqual(LocalDate.now());
    }
}
