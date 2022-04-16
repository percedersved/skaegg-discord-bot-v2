package se.skaegg.discordbot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.jpa.TimerEntity;
import se.skaegg.discordbot.jpa.TimerRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

//@Service Tidigare var det denna annotering
@Component
public class Timer implements SlashCommand {

    TimerRepository timerRepository;

    public Timer(TimerRepository timerRepository) {
        this.timerRepository = timerRepository;
    }


    @Override
    public String getName() {
        return "nedräkning";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        ApplicationCommandInteractionOption subCommand = event.getOptions().get(0);
        String subCommandName = subCommand.getName();
        List<ApplicationCommandInteractionOption> subCommandOptions = subCommand.getOptions();

        switch (subCommandName) {
            case "visa":
                return checkTimer(event, subCommandOptions);
            case "skapa":
                return createTimer(event, subCommandOptions);
            case "lista":
                return listAllTimers(event);
            case "tabort":
                return deleteTimer(event, subCommandOptions);
            default:
                return Mono.empty();
        }
    }

    private Mono<Void> createTimer(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {

        boolean timerAlreadyExists = false;
        boolean noKeyAdded = false;

        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String timerKey = options.get(0).getValue().get().getRaw();
        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String timerDateTimeString = options.get(1).getValue().get().getRaw();

        if (!timerRepository.findByKeyIgnoreCase(timerKey).isEmpty()) {
            timerAlreadyExists = true;
        } else if ((timerKey == null || timerKey.isBlank()) || timerDateTimeString.isBlank()) {
            noKeyAdded = true;
        } else {
            // Do all the DB stuff
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime timerDateTime = LocalDateTime.parse(timerDateTimeString, formatter);
            TimerEntity timer = new TimerEntity();
            timer.setKey(timerKey);
            timer.setTimeDateTime(timerDateTime);
            timer.setProcessed(false);
            Snowflake originChannel = Objects.requireNonNull(event.getInteraction().getChannel().doOnSuccess(MessageChannel::getId).block()).getId();
            timer.setChannelId(originChannel.asString());
            timerRepository.save(timer);
        }

        boolean finalTimerAlreadyExists = timerAlreadyExists;
        boolean finalNoKeyAdded = noKeyAdded;
        if (finalTimerAlreadyExists) {
            return event.reply()
                    .withContent("Doh! Det finns redan en nedräkning med det namnet, testa ett annat namn");
        } else if (finalNoKeyAdded) {
            return event.reply()
                    .withContent("Du måste ange både ett namn på nedräkningen följt av komma och ett datum i formatet yyyy-MM-dd HH:mm");
        } else {
            return event.reply()
                    .withContent("Nedräkning med namn " + timerKey + " och datum " + timerDateTimeString + " har lagts till");
        }
    }


    private Mono<Void> checkTimer(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {

        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String timerName = options.get(0).getValue().get().getRaw();

        List<TimerEntity> timers = timerRepository.findByKeyIgnoreCase(timerName);
        String timeLeft = null;
        TimerEntity timer = null;
        long diff = 0;


        if (!timers.isEmpty()) {
            timer = timers.get(0);
            LocalDateTime expirationDate = timer.getTimeDateTime();

            Duration duration = Duration.between(LocalDateTime.now(), expirationDate);
            diff = duration.toMinutes();


            if (diff >= 1440) {
                timeLeft = diff / 24 / 60 + " dagar, " + diff / 60 % 24 + "h, " + diff % 60 + "m";
            } else if (diff >= 60) {
                timeLeft = diff / 60 + "h, " + diff % 60 + "m";
            } else {
                timeLeft = diff + "m";
            }
            timeLeft = timeLeft + " kvar till " + timer.getKey();
        }


        if (diff == 0 || timer == null) {
            return event.reply()
                    .withContent("Det finns ingen nedräkning med det namnet");
        } else if (diff >= 0) {
            return event.reply()
                    .withContent(timeLeft);
        } else {
            return event.reply()
                    .withContent("Nedräkning passerad");
        }
    }


    public Mono<Void> listAllTimers(ChatInputInteractionEvent event) {
        List<TimerEntity> timers = timerRepository.findByProcessed(false);

        StringBuilder sb = new StringBuilder();
        sb.append("Namn")
                .append("\t")
                .append("ID")
                .append("\n");
        for (TimerEntity timer : timers) {
            sb.append(timer.getKey())
                    .append("\t")
                    .append(timer.getId())
                    .append("\n");
        }
        String availableTimers = sb.toString();

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Nedräkningar")
                .description(availableTimers)
                .build();

        return event.reply()
                .withEmbeds(embed);
    }


    public Mono<Void> deleteTimer(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {

        @SuppressWarnings("OptionalGetWithoutIsPresent") // Option is required, will always be present
        String idToDeleteString = options.get(0).getValue().get().getRaw();
        Integer idToDelete = Integer.parseInt(idToDeleteString);

        if (timerRepository.findById(idToDelete).isPresent()) {
            timerRepository.deleteById(idToDelete);
            return event.reply()
                    .withContent("Nedräkning med ID " + idToDelete + " raderades");
        }
        else {
            return event.reply()
                    .withContent("Det finns ingen nedräkning med det IDt");
        }
    }
}
