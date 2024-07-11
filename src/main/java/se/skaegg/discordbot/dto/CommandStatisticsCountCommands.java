package se.skaegg.discordbot.dto;

public class CommandStatisticsCountCommands {
    String commandName;
    Long countId;

    public CommandStatisticsCountCommands(String commandName, Long countId) {
        this.commandName = commandName;
        this.countId = countId;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public Long getCountId() {
        return countId;
    }

    public void setCountId(Long countId) {
        this.countId = countId;
    }
}
