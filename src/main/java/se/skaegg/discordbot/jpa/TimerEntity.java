package se.skaegg.discordbot.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "timers")
public class TimerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "timer_key", unique = true, nullable = false)
    String key;

    @Column(name = "timer_date_time")
    LocalDateTime timeDateTime;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getTimeDateTime() {
        return timeDateTime;
    }

    public void setTimeDateTime(LocalDateTime timeDateTime) {
        this.timeDateTime = timeDateTime;
    }

    public Boolean getProcessed() {
        return processed;
    }

    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
