package se.skaegg.discordbot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.clients.QuickChartClient;
import se.skaegg.discordbot.jpa.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Poll extends AbstractMessageHandler implements SlashCommand {

    PollsRepository pollsRepository;
    PollAlternativesRepository pollAlternativesRepository;
    PollVotesRepository pollVotesRepository;

    public Poll(PollsRepository pollsRepository, PollAlternativesRepository pollAlternativesRepository, PollVotesRepository pollVotesRepository) {
        this.pollsRepository = pollsRepository;
        this.pollAlternativesRepository = pollAlternativesRepository;
        this.pollVotesRepository = pollVotesRepository;
    }

    @Override
    public String getName() {
        return "omröstning";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        ApplicationCommandInteractionOption subCommand = event.getOptions().get(0);
        String subCommandName = subCommand.getName();
        List<ApplicationCommandInteractionOption> subCommandOptions = subCommand.getOptions();

        switch (subCommandName) {
            case "skapa" -> { return createPoll(event, subCommandOptions); }
            case "visa" -> { return presentListOfPolls(event, subCommandOptions); }
            case "ta_bort" -> { //TODO: Maybe add this?
            }
            default -> {
                return Mono.empty();
            }
        }
        return Mono.empty();
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Mono<Void> createPoll(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {
        deferEventReply(event, true);

        String name = options.get(0).getValue().get().getRaw();
        String endDateString = options.get(1).getValue().get().getRaw();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate endDate = LocalDate.parse(endDateString, formatter);
        // Get answers option comma separated String and split it to a list of Strings
        List<String> answers = Arrays.stream(options.get(2).getValue().get().getRaw().split(" *, *")).toList();

        PollsEntity pollsEntity = new PollsEntity();
        pollsEntity.setName(name);
        pollsEntity.setEndDate(endDate);
        pollsEntity.setUserId(event.getInteraction().getUser().getId().asString());
        Snowflake originChannel = Objects.requireNonNull(event.getInteraction().getChannel().doOnSuccess(MessageChannel::getId).block()).getId();
        pollsEntity.setChannelId(originChannel.asString());
        pollsEntity.setProcessed(false);
        pollsRepository.save(pollsEntity);

        answers.forEach(answer -> {
            PollAlternativesEntity pollAlternativesEntity = new PollAlternativesEntity();
            pollAlternativesEntity.setPollId(pollsEntity);
            pollAlternativesEntity.setValue(answer);
            pollAlternativesRepository.save(pollAlternativesEntity);
        });

        event.editReply("Omröstningen har lagts till!").subscribe();
        return Mono.empty();
    }

    private Mono<Void> presentListOfPolls(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> options) {
        deferEventReply(event, false);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        String searchPhrase = options.get(0).getValue().get().getRaw();

        List<PollsEntity> searchResults = pollsRepository.findByNameContainingAndProcessed(searchPhrase, false);
        if (searchResults.isEmpty()) {
            return event.editReply("Ingen omröstning med din söksträng i namnet hittades")
                   .then();
        }

        List<SelectMenu.Option> pollsList = new ArrayList<>();
        searchResults.forEach(sr -> pollsList.add(SelectMenu.Option.of(sr.getName(), sr.getId().toString())));

        return event.editReply()
                .withComponents(ActionRow.of(SelectMenu.of("polls", pollsList))).then();
    }

    public Mono<Message> showPoll(SelectMenuInteractionEvent event) {
        int pollId = Integer.parseInt(event.getValues().get(0));
        PollsEntity poll = pollsRepository.findById(pollId);

        List<PollAlternativesEntity> pollAlternatives = pollAlternativesRepository.findByPollId(poll);

        List<Button> alternativeButtons = new ArrayList<>();
        pollAlternatives.forEach(pa -> {
            Button b = Button.primary("poll_" + poll.getId() + "_" + pa.getId().toString(), pa.getValue());
            alternativeButtons.add(b);
        });
        List<ActionRow> alternativeActionRows = new ArrayList<>();
        fillActionRowList(alternativeButtons.iterator(), alternativeActionRows);

        EmbedCreateSpec chartEmbed = createPollChartEmbedSpec(poll, poll.getName());

        event.reply()
                .withComponents(List.copyOf(alternativeActionRows)) // This doesn't work without List.copyOf which makes the list immutable
                .withEmbeds(chartEmbed)
                .retry(3)
                .subscribe();

        event.getInteraction()
                .getMessage()
                .get()
                .edit()
                .withComponents(ActionRow.of(SelectMenu.of("disabled", SelectMenu.Option.ofDefault(poll.getName(), "disabled"))
                        .disabled()))
                .subscribe();
        return Mono.empty();
    }

    /**
     * Only 5 buttons are allowed in 1 ActionRow.
     * This method starts an action row and fills it with 5 buttons.
     * If there are more than 5 buttons it will create a new {@link discord4j.core.object.component.ActionRow ActionRow}
     *
     * @param buttonListIterator Iterator of all buttons that should be in the ActionRows
     * @param actionRowListToFill Empty List of ActionRow to fill
     */
    private void fillActionRowList(Iterator<Button> buttonListIterator, List<ActionRow> actionRowListToFill) {
        int counter = 0;
        List<Button> buttons = new ArrayList<>();
        while (buttonListIterator.hasNext() && counter <= 4) {
            buttons.add(buttonListIterator.next());
            counter++;
        }
        actionRowListToFill.add(ActionRow.of(buttons));
        if (buttonListIterator.hasNext()) {
            fillActionRowList(buttonListIterator, actionRowListToFill);
        }
    }

    public EmbedCreateSpec createPollChartEmbedSpec(PollsEntity poll, String title) {
        List<PollVotesPerPollId> votes = pollVotesRepository.countVotesPerPollId(poll);
        Map<String, Long> votesMap = new LinkedHashMap<>(); // Using LinkedHashMap here to make the alternatives come in the same order they were added
        votes.forEach(vote -> votesMap.put(vote.getAlternativeName(), vote.getVoteCount()));

        QuickChartClient qcClient = new QuickChartClient();

        return EmbedCreateSpec.builder()
                .title(title)
                .color(Color.of(90, 130, 180))
                .image(qcClient.createPollChart(poll.getName(), votesMap))
                .build();
    }

    public void addVote(ButtonInteractionEvent event) {
        deferEventReply(event, true);

        String interactionUser = event.getInteraction().getUser().getId().asString();

        int alternativeId = 0;
        int pollId = 0;
        Matcher m = Pattern.compile("poll_(\\d+)_(\\d+)").matcher(event.getCustomId());
        if (m.find()) {
            pollId = Integer.parseInt(m.group(1));
            alternativeId = Integer.parseInt(m.group(2));
        }

        PollsEntity poll = pollsRepository.findById(pollId);
        PollAlternativesEntity alternative = pollAlternativesRepository.findById(alternativeId);

        if (pollVotesRepository.findByAlternativeIdAndUserId(alternative, interactionUser) != null) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Du har redan lagt en röst på det alternativet")
                    .subscribe();
            return;
        }

        PollVotesEntity vote = new PollVotesEntity();
        vote.setAlternativeId(alternative);
        vote.setPollId(poll);
        vote.setUserId(event.getInteraction().getUser().getId().asString());
        pollVotesRepository.save(vote);

        event.createFollowup()
                .withEphemeral(true)
                .withContent("Tack för din röst!")
                .subscribe();
    }
}
