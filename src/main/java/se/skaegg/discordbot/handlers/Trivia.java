package se.skaegg.discordbot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ComponentData;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;
import se.skaegg.discordbot.clients.OpenTriviaClient;
import se.skaegg.discordbot.clients.TheTriviaApiClient;
import se.skaegg.discordbot.dto.OpenTriviaObject;
import se.skaegg.discordbot.dto.OpenTriviaResults;
import se.skaegg.discordbot.dto.TheTriviaApiResults;
import se.skaegg.discordbot.jpa.*;
import se.skaegg.discordbot.listeners.TriviaTempListener;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class Trivia implements SlashCommand {


    TriviaQuestionsRepository triviaQuestionsRepository;
    TriviaScoresRepository triviaScoresRepository;
    TriviaButtonClicksRepository triviaButtonClicksRepository;
    GatewayDiscordClient client;

    private static final Logger LOG = LoggerFactory.getLogger(Trivia.class);

    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;
    @Value("${trivia.results.maxresults}")
    Integer maxResults;

    @Value("#{'${serverIds}'.split(',')}")
    List<String> serverIds;


    public Trivia(TriviaQuestionsRepository triviaQuestionsRepository,
                  TriviaScoresRepository triviaScoresRepository,
                  TriviaButtonClicksRepository triviaButtonClicksRepository,
                  GatewayDiscordClient client) {
        this.triviaQuestionsRepository = triviaQuestionsRepository;
        this.triviaScoresRepository = triviaScoresRepository;
        this.triviaButtonClicksRepository = triviaButtonClicksRepository;
        this.client = client;
    }

    @Override
    public String getName() {
        return "trivia";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        ApplicationCommandInteractionOption subCommand = event.getOptions().get(0);
        String subCommandName = subCommand.getName();

        switch (subCommandName) {
            case "dagens":
                event.reply("Dagens fråga").subscribe();
                String manualChannelId = event.getInteraction().getChannelId().asString();
                return createGetQuestionButton(manualChannelId);
            case "ställning_innevarande":
                return showStandings(event, scoresPeriod.CURRENT_MONTH);
            case "ställning_föregående":
                return showStandings(event, scoresPeriod.PREVIOUS_MONTH);
            case "ställning_alltime":
                return showStandings(event, scoresPeriod.ALL_TIME);
            case "andel_svar_månad":
                List<ApplicationCommandInteractionOption> subCommandOptions = subCommand.getOptions();
                return displayAnswersPerUserAndMonth(event, subCommandOptions);
            default:
                return Mono.empty();
        }
    }


    public Mono<Void> createGetQuestionButton(String channelId) {
        client.getChannelById(Snowflake.of(channelId))
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage()
                        .withContent("Klicka på \"Hämta\" för att hämta frågan. Du har 15 sekunder på dig att svara efter att du fått frågan")
                        .withComponents(ActionRow.of(
                                        Button.secondary("getQuestion_" + LocalDate.now(), "Hämta")
                                )
                        )
                )
                .retry(3)
                .subscribe();
        return Mono.empty();
    }

    public Mono<Void> createQuestions(String url, String queryParams, String source, LocalDate date, ButtonInteractionEvent event) {
        event.deferReply()
                .withEphemeral(true)
                .retry(3)
                .onErrorResume(e -> {
                    if (e instanceof ClientException) {
                        LOG.error("Discord4j ClientException: \n{}", e.getMessage());
                    }
                    else if (e instanceof PrematureCloseException) {
                        LOG.error("Netty PrematureCloseExcption, something closed the connection: \n{}", e.getMessage());
                    }
                    else {
                        LOG.error("An error occured but was not ClientException or PrematureCloseException\n{}", e.getMessage());
                    }
                    return Mono.empty();
                }).subscribe();

        // If source is opentdb and no question has been fetched from the API today, get new question and save to DB
        if (source.equals("opentdb") && triviaQuestionsRepository.findByQuestionDate(date) == null) {
            OpenTriviaObject triviaObject = new OpenTriviaClient(url, queryParams).process();
            OpenTriviaResults question = triviaObject.getResults().get(0);
            saveQuestionToDb(question);
        }
        // If source is the-trivia-api and no question has been fetched from the API today, get new question and save to DB
        else if (source.equals("the-trivia-api") && triviaQuestionsRepository.findByQuestionDate(date) == null) {
            TheTriviaApiResults question = new TheTriviaApiClient(url, queryParams).process();
            saveTheTriviaApiQuestionToDb(question);
        }
        else {
            LOG.error("Couldnt determine if should use opentdb or the-trivia-api to get the question. Check property trivia.source");
        }

        TriviaQuestionsEntity questionsEntity = triviaQuestionsRepository.findByQuestionDate(date);
        int questionId = questionsEntity.getId();
        String correctAnswer = questionsEntity.getCorrectAnswer();
        List<String> incorrectAnswers = questionsEntity.getIncorrectAnswers();

        // Check if the user already fetched todays question. You may only do this once
        String interactionUser = event.getInteraction().getUser().getId().asString();
        if (triviaButtonClicksRepository.findByUserIdAndQuestion(interactionUser, questionsEntity) != null) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Du har redan hämtat dagens fråga")
                    .subscribe();
            return Mono.empty();
        }

        // Add incorrect answers to Map with the answer as key and as the value. The key is also unescaped to remove HTML encoded tags
        Map<String, String> answersMap = incorrectAnswers.stream()
                .collect(Collectors.toMap(HtmlUtils::htmlUnescape, answer -> questionId + "_trivia_" +  answer));
        // Add the correct answer to the map with the answer as key and "trivia_correct_answer" as value
        answersMap.put(correctAnswer, questionId + "_trivia_correct_answer");

        // Add all the keys from the map to a list to be able to access them by index
        List<String> allAnswers = new ArrayList<>(answersMap.keySet());
        // Shuffle the list so the correct answer isn't always in the same place
        Collections.shuffle(allAnswers);


        if (answersMap.size() == 4) {
            event.createFollowup()
                    .withContent(questionsEntity.getQuestion())
                    .withEphemeral(true)
                    .withComponents(ActionRow.of(
                            // The name of the button is fetched from the shuffled list and the customId is fetched from the corresponding value in the map
                            // So the correct answer will have "trivia_correct_answer" as customId
                            Button.primary(answersMap.get(allAnswers.get(0)), allAnswers.get(0)),
                            Button.primary(answersMap.get(allAnswers.get(1)), allAnswers.get(1)),
                            Button.primary(answersMap.get(allAnswers.get(2)), allAnswers.get(2)),
                            Button.primary(answersMap.get(allAnswers.get(3)), allAnswers.get(3))
                            )
                    )
                    .retry(3)
                    .then(new TriviaTempListener().createTempListener(this, client))
                    .subscribe();
        }
        else if (allAnswers.size() == 2) {
            event.createFollowup()
                    .withContent(questionsEntity.getQuestion())
                    .withEphemeral(true)
                    .withComponents(ActionRow.of(
                            Button.primary(answersMap.get(allAnswers.get(0)), allAnswers.get(0)),
                            Button.primary(answersMap.get(allAnswers.get(1)), allAnswers.get(1))
                            )
                    )
                    .retry(3)
                    .then(new TriviaTempListener().createTempListener(this, client))
                    .subscribe();
        }
        else {
            LOG.error("Something went wrong with the answers. There should only be 2 or 4 answers in the API response. Number of answers were {}", allAnswers.size());
            return Mono.empty();
        }

        // Save user and question to button click table so we know that the fetch button has been pushed for this question
        TriviaButtonClicksEntity triviaButtonClicksEntity = new TriviaButtonClicksEntity();
        triviaButtonClicksEntity.setUserId(interactionUser);
        triviaButtonClicksEntity.setQuestion(questionsEntity);
        triviaButtonClicksEntity.setDateTimeClicked(LocalDateTime.now());
        triviaButtonClicksRepository.save(triviaButtonClicksEntity);

        return Mono.empty();
    }


    public Mono<Void> checkAnswer(ButtonInteractionEvent event) {

        event.deferReply()
                .withEphemeral(true)
                .retry(3)
                .onErrorResume(e -> {
                    if (e instanceof ClientException) {
                        LOG.error("Discord4j ClientException: \n{}", e.getMessage());
                    }
                    else if (e instanceof PrematureCloseException) {
                        LOG.error("Netty PrematureCloseExcption, something closed the connection: \n{}", e.getMessage());
                    }
                    else {
                        LOG.error("An error occured but was not ClientException or PrematureCloseException\n{}", e.getMessage());
                    }
                    return Mono.empty();
                }).subscribe();

        String interactionUser = event.getInteraction().getUser().getId().asString();

        int questionId = 0;
        Matcher m = Pattern.compile("(.*?)_").matcher(event.getCustomId());
        if (m.find()) {
            questionId = Integer.parseInt(m.group(1));
        }

        TriviaQuestionsEntity question = triviaQuestionsRepository.findById(questionId);

        if (triviaScoresRepository.findByUserIdAndQuestion(interactionUser, question) != null) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Du har redan svarat på den frågan")
                    .subscribe();
            return Mono.empty();
        }

        else if (Duration.between(triviaButtonClicksRepository.findByUserIdAndQuestion(interactionUser, question).getDateTimeClicked(), LocalDateTime.now()).getSeconds() > 15L) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Dina 15 sekunder har gått så nu får du inte svara. Du får snabba på lite nästa gång")
                    .subscribe();
            return Mono.empty();
        }

        else {
            Map<String, String> answersMap = new HashMap<>();
            // Get information about the buttons
            List<ComponentData> compData = event.getMessage()
                    .get()
                    .getComponents()
                    .get(0)
                    .getData()
                    .components()
                    .get();

            // Get CustomId and Label och the buttons and put them in a map
            for (ComponentData data : compData) {
                answersMap.put(data.customId().get(), data.label().get());
            }

            String correctAnswerCustomId = questionId + "_trivia_correct_answer";
            String correctAnswerLabel = answersMap.get(correctAnswerCustomId);

            String userId = event.getInteraction().getUser().getId().asString();

            Snowflake channelIdSnowFlake = Objects.requireNonNull(event.getMessage().get().getChannel().block()).getId();

            TriviaScoresEntity scoresEntity = new TriviaScoresEntity();
            scoresEntity.setAnswerDate(LocalDate.now());
            scoresEntity.setQuestion(question);
            scoresEntity.setUserId(userId);

            LOG.debug("{} pushed an answer button with customID: {}. Now we will try to find out if the answer was right or wrong", userId, event.getCustomId());

            if (event.getCustomId().equals(correctAnswerCustomId)) {
                LOG.debug("The answer was correct");
                scoresEntity.setCorrectAnswer(true);
                event.createFollowup() // This needs to be here since discord awaits a response/followup, otherwise the bot will show "thinking" forever
                        .withContent("Snyggt, du svarade rätt!")
                        .withEphemeral(true)
                        .retry(3)
                        .subscribe();
                LOG.debug("The followup has been created for the correct answer");
                client.getChannelById(channelIdSnowFlake) // This needs to be done with client since you cant mix ephemeral responses with normal
                        .ofType(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage()
                                .withContent(":green_circle: <@" + userId + "> svarade rätt på en fråga"))
                        .subscribe();
                LOG.debug("The public message created with client has been sent");
            } else {
                LOG.debug("The answer was incorrect");
                scoresEntity.setCorrectAnswer(false);
                event.createFollowup()
                        .withEphemeral(true)
                        .withContent("Rätt svar var: " + correctAnswerLabel)
                        .retry(3)
                        .subscribe();
                LOG.debug("The followup has been created for the correct answer");
                client.getChannelById(channelIdSnowFlake) // This needs to be done with client since you cant mix ephemeral responses with normal
                        .ofType(MessageChannel.class)
                        .flatMap(channel -> channel.createMessage()
                                .withContent(":red_circle: <@" + userId + "> svarade fel på en fråga"))
                        .subscribe();
                LOG.debug("The public message created with client has been sent");
            }
            LOG.debug("The checking of the answer is done");
            triviaScoresRepository.save(scoresEntity);
            LOG.debug("The answer has been saved to the database");

            LOG.debug("Everything is done and now we just return Mono.empty");
            return Mono.empty();
        }
    }


    private Mono<Void> showStandings(ChatInputInteractionEvent event, scoresPeriod period) {
        event.deferReply().subscribe();

        LocalDate fromDate = null;
        LocalDate toDate = null;
        String nowShowing = null;

        switch (period) {
            case CURRENT_MONTH:
                fromDate = YearMonth.now().atDay(1);
                toDate = YearMonth.now().atEndOfMonth();
                nowShowing = "Innevarande månad";
                break;
            case PREVIOUS_MONTH:
                fromDate = YearMonth.now().minusMonths(1).atDay(1);
                toDate = YearMonth.now().minusMonths(1).atEndOfMonth();
                nowShowing = "Föregående månad";
                break;
            case ALL_TIME:
                fromDate = LocalDate.of(2022, 1, 1);
                toDate = LocalDate.now().plusDays(1);
                nowShowing = "Forever";
                break;
        }

        List<TriviaScoresCountPoints> triviaScoresCountPoints = triviaScoresRepository.countTotalIdsByAnswerAndDates(fromDate, toDate);


        int numberToShow = Math.min(triviaScoresCountPoints.size(), maxResults); // This is just to know what to use the looping. If there are more than {maxResults} rows returned from DB {maxResults} should be max

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberToShow ; i++){
            switch(i) {
                case 0:
                    sb.append(":first_place:");
                    break;
                case 1:
                    sb.append(":second_place:");
                    break;
                case 2:
                    sb.append(":third_place:");
                    break;
                default:
                    sb.append(i + 1);
            }
            sb.append(". ");
            sb.append("<@");
            sb.append(triviaScoresCountPoints.get(i).getUserId());
            sb.append(">");
            sb.append(" - ");
            sb.append(triviaScoresCountPoints.get(i).getPoints());
            sb.append("\n");
        }

        if (numberToShow == 0) {
            return event.createFollowup("Det finns ingen ställning för den perioden än").then();
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Ställning " + nowShowing)
                .addField(EmbedCreateFields.Field.of("Användare - Poäng", sb.toString(), true))
                .build();

        return event.createFollowup().withEmbeds(embed).then();
    }


    private void saveQuestionToDb(OpenTriviaResults question) {
        String correctAnswer = HtmlUtils.htmlUnescape(question.getCorrectAnswer());
        List<String> incorrectAnswers = question.getIncorrectAnswers();

        TriviaQuestionsEntity entity = new TriviaQuestionsEntity();
        entity.setQuestion(HtmlUtils.htmlUnescape(question.getQuestion()));
        entity.setCorrectAnswer(correctAnswer);
        entity.setCategory(question.getCategory());
        entity.setDifficulty(question.getDifficulty());
        entity.setType(question.getType());
        entity.setQuestionDate(LocalDate.now());

        if (question.getType().equals("multiple")) {
            entity.setIncorrectAnswer1(incorrectAnswers.get(0));
            entity.setIncorrectAnswer2(incorrectAnswers.get(1));
            entity.setIncorrectAnswer3(incorrectAnswers.get(2));
        }
        else {
            entity.setIncorrectAnswer1(incorrectAnswers.get(0));
        }

        triviaQuestionsRepository.save(entity);
    }


    private void saveTheTriviaApiQuestionToDb(TheTriviaApiResults question) {
        String correctAnswer = HtmlUtils.htmlUnescape(question.getCorrectAnswer());
        List<String> incorrectAnswers = question.getIncorrectAnswers();

        TriviaQuestionsEntity entity = new TriviaQuestionsEntity();
        entity.setQuestion(HtmlUtils.htmlUnescape(question.getQuestion()));
        entity.setCorrectAnswer(correctAnswer);
        entity.setCategory(question.getCategory());
        entity.setDifficulty(question.getDifficulty());
        entity.setType(question.getType());
        entity.setQuestionDate(LocalDate.now());

        if (question.getType().equals("Multiple Choice")) {
            entity.setIncorrectAnswer1(incorrectAnswers.get(0));
            entity.setIncorrectAnswer2(incorrectAnswers.get(1));
            entity.setIncorrectAnswer3(incorrectAnswers.get(2));
        }
        else {
            entity.setIncorrectAnswer1(incorrectAnswers.get(0));
        }

        triviaQuestionsRepository.save(entity);
    }

    public void displayCorrectAnswerPercentForDate(LocalDate date, String channelId) {

        TriviaPercentageForDateEntity percentageForDate = triviaScoresRepository.percentageCorrectByDate2(date);
        String percentageFormatted = String.format("%.1f", percentageForDate.getPercentCorrect());

        String text = percentageFormatted +
                "% svarade rätt på gårdagens fråga: \n*" +
                percentageForDate.getQuestion() +
                "*";

        client.getChannelById(Snowflake.of(channelId))
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage()
                        .withContent(text)
                )
                .retry(3)
                .subscribe();
    }


    public Mono<Void> displayAnswersPerUserAndMonth(ChatInputInteractionEvent event, List<ApplicationCommandInteractionOption> subCommandOptions) {
        event.deferReply().subscribe();

        String year = subCommandOptions.get(0).getValue().get().getRaw();
        String month = subCommandOptions.get(1).getValue().get().getRaw();
        LocalDate start = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
        LocalDate end = start.withDayOfMonth(start.getMonth().length(start.isLeapYear()));
        List<TriviaAnswersPerUserMonth> answersPerUserMonth = triviaScoresRepository.answersPerUserAndMonth(start, end);

        StringBuilder userBuilder = new StringBuilder();
        StringBuilder pointsBuilder = new StringBuilder();
        StringBuilder percentBuilder = new StringBuilder();

        for (TriviaAnswersPerUserMonth row : answersPerUserMonth) {
            String user = Objects.requireNonNull(client.
                    getMemberById(event.getInteraction().getGuildId().get(), Snowflake.of(row.getUserId())).block()).getDisplayName();
            userBuilder.append(user);
            userBuilder.append("\n");
            pointsBuilder.append(row.getPoints());
            pointsBuilder.append("\n");
            percentBuilder.append(row.getPercent());
            percentBuilder.append("%\n");
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Andel svar för månad " + month)
                .addField(EmbedCreateFields.Field.of("Användare", userBuilder.toString(), true))
                .addField(EmbedCreateFields.Field.of("Poäng", pointsBuilder.toString(), true))
                .addField(EmbedCreateFields.Field.of("Andel svarade frågor", percentBuilder.toString(), true))
                .build();

        event.createFollowup()
                .withEmbeds(embed)
                .subscribe();

        return Mono.empty();
    }


    enum scoresPeriod {
        CURRENT_MONTH,
        PREVIOUS_MONTH,
        ALL_TIME
    }
}
