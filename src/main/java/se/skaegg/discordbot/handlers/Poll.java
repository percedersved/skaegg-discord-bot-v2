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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(Poll.class);
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
            case "ta_bort" -> {

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

        List<PollsEntity> searchResults = pollsRepository.findByNameContaining(searchPhrase);

        List<SelectMenu.Option> pollsList = new ArrayList<>();
        if (searchResults.isEmpty()) {
            return Mono.empty();
        }
        searchResults.forEach(sr -> pollsList.add(SelectMenu.Option.of(sr.getName(), sr.getId().toString())));

        return event.editReply()
                .withComponents(ActionRow.of(SelectMenu.of("polls", pollsList))).then();
    }

    public Mono<Message> showPoll(SelectMenuInteractionEvent event) {
        String pollId = event.getValues().get(0);
        PollsEntity poll = pollsRepository.findById(Integer.parseInt(pollId));

        if (poll == null) {
            event.reply("Hittade ingen omröstning med det namnet").subscribe();
            return Mono.empty();
        }
        List<PollAlternativesEntity> pollAlternatives = pollAlternativesRepository.findByPollId(poll);

        List<Button> alternativeButtons = new ArrayList<>();
        pollAlternatives.forEach(pa -> {
            Button b = Button.primary("poll_" + poll.getId() + "_" + pa.getId().toString(), pa.getValue());
            alternativeButtons.add(b);
        });

        List<PollVotesPerPollId> votes = pollVotesRepository.countVotesPerPollId(poll);
        Map<String, Long> votesMap = new HashMap<>();
        for (PollVotesPerPollId vote : votes) {
            votesMap.put(vote.getAlternativeName(), vote.getVoteCount());
        }

        QuickChartClient qcClient = new QuickChartClient();
        EmbedCreateSpec chartEmbed = EmbedCreateSpec.builder()
                .image(qcClient.createPollChart(poll.getName(), votesMap))
                .build();

        event.reply().withContent(poll.getName())
                .withComponents(ActionRow.of(alternativeButtons))
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
