package se.skaegg.discordbot.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "command_statistics")
public class CommandStatisticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Integer id;

    @Column(name = "command_name")
    String commandName;

    @Column(name = "called_by_user_id")
    String calledByUserId;

    @Column(name = "original_channel_id")
    String originalChannelId;

    @Column(name = "command_date_time")
    LocalDateTime commandDateTime;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getCalledByUserId() {
        return calledByUserId;
    }

    public void setCalledByUserId(String calledByUserId) {
        this.calledByUserId = calledByUserId;
    }

    public String getOriginalChannelId() {
        return originalChannelId;
    }

    public void setOriginalChannelId(String originalChannelId) {
        this.originalChannelId = originalChannelId;
    }

    public LocalDateTime getCommandDateTime() {
        return commandDateTime;
    }

    public void setCommandDateTime(LocalDateTime commandDateTime) {
        this.commandDateTime = commandDateTime;
    }
}
