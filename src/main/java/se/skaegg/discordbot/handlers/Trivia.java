package se.skaegg.discordbot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
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
import se.skaegg.discordbot.dto.TriviaResults;
import se.skaegg.discordbot.jpa.*;
import se.skaegg.discordbot.listeners.TriviaTempListener;

import java.time.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class Trivia implements SlashCommand {


    TriviaQuestionsRepository triviaQuestionsRepository;
    TriviaScoresRepository triviaScoresRepository;
    TriviaButtonClicksRepository triviaButtonClicksRepository;
    private Thread answerTimerThread;
    GatewayDiscordClient client;
    String interactionUser;
    int numOfCharsQuestionsAnswers;

    private static final Logger LOG = LoggerFactory.getLogger(Trivia.class);
    private static final int ANSWERING_TIME_LIMIT = 13; // Seconds
    private static final Long ANSWERING_TIME_REMINDER = 5000L; // Milliseconds
    private static final String ANSWERING_TIME_REMINDER_TEXT = "Du har endast 5 sekunder kvar, synda synda!";

    @Value("${trivia.results.maxresults}")
    Integer maxResults;


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
            case "dagens" -> {
                event.reply("Dagens fråga").subscribe();
                String manualChannelId = event.getInteraction().getChannelId().asString();
                return createGetQuestionButton(manualChannelId);
            }
            case "ställning" -> {
                String period = subCommand.getOptions()
                        .get(0)
                        .getValue()
                        .map(ApplicationCommandInteractionOptionValue::asString)
                        .orElseThrow();
                return showStandings(event, scoresPeriod.valueOf(period));
            }
            case "andel_svar_månad" -> {
                List<ApplicationCommandInteractionOption> subCommandOptions = subCommand.getOptions();
                return displayAnswersPerUserAndMonth(event, subCommandOptions);
            }
            default -> {
                return Mono.empty();
            }
        }
    }


    public Mono<Void> createGetQuestionButton(String channelId) {
        client.getChannelById(Snowflake.of(channelId))
                .ofType(MessageChannel.class)
                .flatMap(channel -> channel.createMessage()
                        .withContent("Klicka på \"Hämta\" för att hämta frågan. " +
                                "Du har en begränsad tid på dig att svara efter att du fått frågan, en varning kommer när du har 5 sekunder på dig")
                        .withComponents(ActionRow.of(
                                        Button.danger("getQuestion_" + LocalDate.now(), "Hämta")
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

        // Get question from API, check that no answers are over 80 characters and save to DB
        checkAndSaveQuestion(source, date, url, queryParams);

        TriviaQuestionsEntity questionsEntity = triviaQuestionsRepository.findByQuestionDate(date);
        int questionId = questionsEntity.getId();
        String correctAnswer = questionsEntity.getCorrectAnswer();
        List<String> incorrectAnswers = questionsEntity.getIncorrectAnswers();

        // Check if the user already fetched today's question. You may only do this once
        interactionUser = event.getInteraction().getUser().getId().asString();
        if (triviaButtonClicksRepository.findByUserIdAndQuestion(interactionUser, questionsEntity) != null) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Du har redan hämtat dagens fråga. No answering for you! Dagens fråga var: **" + questionsEntity.getQuestion() + "**")
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

        numOfCharsQuestionsAnswers = allAnswers.stream()
                .mapToInt(String::length)
                .sum();

        numOfCharsQuestionsAnswers += questionsEntity.getQuestion().length();


        if (answersMap.size() == 4) {
            event.createFollowup()
                    .withContent(String.format("""
                                    **%s**
                                    
                                    :regional_indicator_a:  %s
                                    :regional_indicator_b:  %s
                                    :regional_indicator_c:  %s
                                    :regional_indicator_d:  %s
                                    """,
                                questionsEntity.getQuestion(), allAnswers.get(0), allAnswers.get(1), allAnswers.get(2), allAnswers.get(3)))
                    .withEphemeral(true)
                    .withComponents(ActionRow.of(
                            // The name of the button is set to A,B,C or D and the customId is fetched from the corresponding value in the map
                            // So the correct answer will have "trivia_correct_answer" as customId
                            Button.primary(answersMap.get(allAnswers.get(0)), "A"),
                            Button.primary(answersMap.get(allAnswers.get(1)), "B"),
                            Button.primary(answersMap.get(allAnswers.get(2)), "C"),
                            Button.primary(answersMap.get(allAnswers.get(3)), "D")
                            )
                    )
                    .retry(3)
                    .then(new TriviaTempListener().createTempListener(this, client))
                    .subscribe();

            startAnsweringTimer(event, ANSWERING_TIME_REMINDER, numOfCharsQuestionsAnswers);
        }
        else if (allAnswers.size() == 2) {
            event.createFollowup()
                    .withContent(String.format("""
                                    **%s**
                                    
                                    :regional_indicator_a:  %s
                                    :regional_indicator_b:  %s
                                    """,
                                questionsEntity.getQuestion(), allAnswers.get(0), allAnswers.get(1)))
                    .withEphemeral(true)
                    .withComponents(ActionRow.of(
                            Button.primary(answersMap.get(allAnswers.get(0)), "A"),
                            Button.primary(answersMap.get(allAnswers.get(1)), "A")
                            )
                    )
                    .retry(3)
                    .then(new TriviaTempListener().createTempListener(this, client))
                    .subscribe();

            startAnsweringTimer(event, ANSWERING_TIME_REMINDER, numOfCharsQuestionsAnswers);
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

    private void startAnsweringTimer(ButtonInteractionEvent event, long timeReminder, int numOfCharsQuestionsAnswers) {

        long totalReminderTime = timeReminder + ((numOfCharsQuestionsAnswers / 10) * 1000L);

        answerTimerThread = new Thread(() -> {
            LOG.debug("new Thread started for answering timer. will wait for {} ms before sending the 5 second reminder", totalReminderTime);
            while (!answerTimerThread.isInterrupted()) {
                try {
                    Thread.sleep(totalReminderTime);
                    event.createFollowup()
                        .withEphemeral(true)
                        .withContent(ANSWERING_TIME_REMINDER_TEXT)
                        .subscribe();
                    stopAnsweringTimer();
                }
                catch (InterruptedException e){
                    // This means that the thread has been interrupted by stopAnsweringTimer which is fine. Just break out of loop;
                    break;
                }
            }
        });
        answerTimerThread.setName("answeringTimerThread");
        answerTimerThread.start();
    }

    public void stopAnsweringTimer() {
        this.answerTimerThread.interrupt();
        LOG.debug("Answering timer thread stopped. Current number of threads in thread group when interrupting current Trivia thread: {}", Thread.activeCount());
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

        long totalTimeLimit = ANSWERING_TIME_LIMIT + (numOfCharsQuestionsAnswers / 10);

        if (triviaScoresRepository.findByUserIdAndQuestion(interactionUser, question) != null) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Du har redan svarat på den frågan")
                    .subscribe();
            return Mono.empty();
        }

        else if (Duration.between(triviaButtonClicksRepository.findByUserIdAndQuestion(interactionUser, question).getDateTimeClicked(), LocalDateTime.now()).getSeconds() > totalTimeLimit) {
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent("Nu var du för långsam, din tid är slut. Du får snabba på lite nästa gång")
                    .subscribe();
            return Mono.empty();
        }

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

        LocalDate questionDate = question.getQuestionDate();
        String publicDisplayStringCorrect = questionDate.equals(LocalDate.now()) ? ":green_circle: <@" + userId + "> " + "svarade rätt på dagens fråga" :
                ":green_circle: <@" + userId + "> " + "svarade rätt på frågan från " + questionDate;
        String publicDisplayStringIncorrect = questionDate.equals(LocalDate.now()) ? ":red_circle: <@" + userId + "> svarade fel på dagens fråga " :
                ":red_circle: <@" + userId + "> svarade fel på frågan från " + questionDate;

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
                            .withContent(publicDisplayStringCorrect))
                    .subscribe();
            LOG.debug("The public message created with client has been sent");
        } else {
            LOG.debug("The answer was incorrect");
            scoresEntity.setCorrectAnswer(false);
            event.createFollowup()
                    .withEphemeral(true)
                    .withContent(String.format("Rätt svar var: %s  %s", correctAnswerLabel,question.getCorrectAnswer()))
                    .retry(3)
                    .subscribe();
            LOG.debug("The followup has been created for the correct answer");
            client.getChannelById(channelIdSnowFlake) // This needs to be done with client since you cant mix ephemeral responses with normal
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage()
                            .withContent(publicDisplayStringIncorrect))
                    .subscribe();
            LOG.debug("The public message created with client has been sent");
        }
        LOG.debug("The checking of the answer is done");
        triviaScoresRepository.save(scoresEntity);
        LOG.debug("The answer has been saved to the database");

        LOG.debug("Everything is done and now we just return Mono.empty");
        return Mono.empty();
    }

    private void checkAndSaveQuestion(String source, LocalDate date, String url, String queryParams) {

        TriviaResults question;

        if (!source.equals("opentdb") && !source.equals("the-trivia-api")) {
            LOG.error("Could not determine if we should use opentdb or the-trivia-api. Check property trivia.source");
            return;
        }
        if (triviaQuestionsRepository.findByQuestionDate(date) != null) {
            LOG.debug("Question already in database for this date, no new question was fetched");
            return;
        }

        if (source.equals("opentdb")) {
            OpenTriviaObject triviaObject = new OpenTriviaClient(url, queryParams).process();
            question = triviaObject.getResults().get(0);
        }
        else {
            question = new TheTriviaApiClient(url, queryParams).process();
        }

        saveQuestionToDb(question);
        LOG.info("Todays question fetched from Trivia API and saved to DB");
    }


    private Mono<Void> showStandings(ChatInputInteractionEvent event, scoresPeriod period) {
        event.deferReply().subscribe();

        LocalDate fromDate = null;
        LocalDate toDate = null;
        String nowShowing = null;

        switch (period) {
            case CURRENT_MONTH -> {
                fromDate = YearMonth.now().atDay(1);
                toDate = YearMonth.now().atEndOfMonth();
                nowShowing = "Innevarande månad";
            }
            case PREVIOUS_MONTH -> {
                fromDate = YearMonth.now().minusMonths(1L).atDay(1);
                toDate = YearMonth.now().minusMonths(1L).atEndOfMonth();
                nowShowing = "Föregående månad";
            }
            case ALL_TIME -> {
                fromDate = LocalDate.of(2022, 1, 1);
                toDate = LocalDate.now().plusDays(1);
                nowShowing = "Forever";
            }
            case CURRENT_YEAR -> {
                fromDate = Year.now().atDay(1);
                toDate = Year.now().atMonth(12).atEndOfMonth();
                nowShowing = "Innevarande år";
            }
            case PREVIOUS_YEAR -> {
                fromDate = Year.now().minusYears(1L).atDay(1);
                toDate = Year.now().minusYears(1L).atMonth(12).atEndOfMonth();
                nowShowing = "Föregående år";
            }
        }

        List<TriviaScoresCountPoints> triviaScoresCountPoints = triviaScoresRepository.countTotalIdsByAnswerAndDates(fromDate, toDate);


        int numberToShow = Math.min(triviaScoresCountPoints.size(), maxResults); // This is just to know what to use the looping. If there are more than {maxResults} rows returned from DB {maxResults} should be max

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberToShow ; i++){
            switch (i) {
                case 0 -> sb.append(":first_place:");
                case 1 -> sb.append(":second_place:");
                case 2 -> sb.append(":third_place:");
                default -> sb.append(i + 1);
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


    private void saveQuestionToDb(TriviaResults question) {
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

    public void displayCorrectAnswerPercentForDate(LocalDate date, String channelId) {

        TriviaPercentageForDateEntity percentageForDate = triviaScoresRepository.percentageCorrectByDate2(date);
        String percentageFormatted = String.format("%.1f", percentageForDate.getPercentCorrect());

        String text = percentageFormatted +
                "% svarade rätt på gårdagens fråga";

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


    public String getInteractionUser() { return interactionUser; }


    enum scoresPeriod {
        CURRENT_MONTH,
        PREVIOUS_MONTH,
        CURRENT_YEAR,
        PREVIOUS_YEAR,
        ALL_TIME
    }
}
