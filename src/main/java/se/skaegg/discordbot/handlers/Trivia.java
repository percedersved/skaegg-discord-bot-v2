package se.skaegg.discordbot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ComponentData;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;
import se.skaegg.discordbot.clients.OpenTriviaClient;
import se.skaegg.discordbot.dto.TriviaObject;
import se.skaegg.discordbot.dto.TriviaResults;
import se.skaegg.discordbot.jpa.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Trivia implements SlashCommand {


    TriviaQuestionsRepository triviaQuestionsRepository;
    TriviaScoresRepository triviaScoresRepository;
    GatewayDiscordClient client;

    private static final Logger log = LoggerFactory.getLogger(Trivia.class);

    @Value("${trivia.daily.channelid}")
    String channelId;
    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;


    public Trivia(TriviaQuestionsRepository triviaQuestionsRepository, TriviaScoresRepository triviaScoresRepository, GatewayDiscordClient client) {
        this.triviaQuestionsRepository = triviaQuestionsRepository;
        this.triviaScoresRepository = triviaScoresRepository;
        this.client = client;
    }

    @Override
    public String getName() {
        return "trivia";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String subCommandName = event.getOptions().get(0).getName();

        switch (subCommandName) {
            case "dagens":
                event.reply("Dagens fråga").subscribe();
                return createQuestions(url, queryParams, channelId).then();
            case "ställning_innevarande":
                return showStandings(event, scoresPeriod.CURRENT_MONTH);
            case "ställning_föregående":
                return showStandings(event, scoresPeriod.PREVIOUS_MONTH);
            case "ställning_alltime":
                return showStandings(event, scoresPeriod.ALL_TIME);
            default:
                return Mono.empty();
        }
    }


    public Mono<Message> createQuestions(String url, String queryParams, String channelId) {
        TriviaObject triviaObject = new OpenTriviaClient(url, queryParams).process();
        TriviaResults question = triviaObject.getResults().get(0);

        // If now question has been fetched from Open Trivia DB today add it to db
        if (triviaQuestionsRepository.findByQuestionDate(LocalDate.now()) == null) {
            saveQuestionToDb(question);
        }

        TriviaQuestionsEntity questionsEntity = triviaQuestionsRepository.findByQuestionDate(LocalDate.now());
        String correctAnswer = questionsEntity.getCorrectAnswer();
        List<String> incorrectAnswers = questionsEntity.getIncorrectAnswers();

        // Add incorrect answers to Map with the answer as key and as the value. The key is also unescaped to remove HTML encoded tags
        Map<String, String> answersMap = incorrectAnswers.stream()
                .collect(Collectors.toMap(HtmlUtils::htmlUnescape, answer -> "trivia_" + answer));
        // Add the correct answer to the map with the answer as key and "trivia_correct_answer" as value
        answersMap.put(correctAnswer, "trivia_correct_answer");

        // Add all the keys from the map to a list to be able to access them by index
        List<String> allAnswers = new ArrayList<>(answersMap.keySet());
        // Shuffle the list so the correct answer isn't always in the same place
        Collections.shuffle(allAnswers);


        if (answersMap.size() == 4) {
            client.getChannelById(Snowflake.of(channelId))
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage()
                            .withContent(questionsEntity.getQuestion())
                            .withComponents(ActionRow.of(
                                            // The name of the button is fetched from the shuffled list and the customId is fetched from the corresponding value in the map
                                            // So the correct answer will have "trivia_correct_answer" as customId
                                            Button.primary(answersMap.get(allAnswers.get(0)), allAnswers.get(0)),
                                            Button.primary(answersMap.get(allAnswers.get(1)), allAnswers.get(1)),
                                            Button.primary(answersMap.get(allAnswers.get(2)), allAnswers.get(2)),
                                            Button.primary(answersMap.get(allAnswers.get(3)), allAnswers.get(3))
                                    )
                            )
                    )
                    .subscribe();
        }
        else if (allAnswers.size() == 2) {
            client.getChannelById(Snowflake.of(channelId))
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage()
                            .withContent(questionsEntity.getQuestion())
                            .withComponents(ActionRow.of(
                                            Button.primary(answersMap.get(allAnswers.get(0)), allAnswers.get(0)),
                                            Button.primary(answersMap.get(allAnswers.get(1)), allAnswers.get(1))
                                    )
                            )
                    );
        }
        else {
            log.error("Something went wrong with the answers. There should only be 2 or 4 answers in the API response. Number of answers were {}", allAnswers.size());
        }
        return Mono.empty();
    }


    public Mono<Void> checkAnswer(ButtonInteractionEvent event) {

        String interactionUser = event.getInteraction().getUser().getUsername();
        if (triviaScoresRepository.findByUserIdAndAnswerDate(interactionUser, LocalDate.now()) != null) {
            event.reply()
                    .withEphemeral(true)
                    .withContent("Du har redan svarat på dagens fråga")
                    .subscribe();
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

            String correctAnswerLabel = answersMap.get("trivia_correct_answer");

            String userId = event.getInteraction().getUser().getId().asString();

            TriviaScoresEntity scoresEntity = new TriviaScoresEntity();
            scoresEntity.setAnswerDate(LocalDate.now());
            scoresEntity.setQuestion(triviaQuestionsRepository.findByQuestionDate(LocalDate.now()));
            scoresEntity.setUserId(interactionUser);
            if (event.getCustomId().equals("trivia_correct_answer")) {
                scoresEntity.setCorrectAnswer(true);
                event.reply()
                        .withContent("<@" + userId + "> svarade rätt")
                        .subscribe();
            } else {
                scoresEntity.setCorrectAnswer(false);
                event.reply().withContent("<@" + userId + "> svarade fel").subscribe();
                event.createFollowup()
                        .withEphemeral(true)
                        .withContent("Rätt svar var: " + correctAnswerLabel)
                        .subscribe();
            }
            triviaScoresRepository.save(scoresEntity);
        }
        return Mono.empty();
    }


    private Mono<Void> showStandings(ChatInputInteractionEvent event, scoresPeriod period) {
        LocalDate fromDate = null;
        LocalDate toDate = null;
        String nowShowing = null;

        switch (period) {
            case CURRENT_MONTH:
                fromDate = YearMonth.now().atDay(1);
                toDate = YearMonth.now().plusMonths(1).atDay(1);
                nowShowing = "Innevarande månad";
                break;
            case PREVIOUS_MONTH:
                fromDate = YearMonth.now().minusMonths(1).atDay(1);
                toDate = YearMonth.now().atDay(1);
                nowShowing = "Föregående månad";
                break;
            case ALL_TIME:
                fromDate = LocalDate.of(2022, 1, 1);
                toDate = LocalDate.now().plusDays(1);
                nowShowing = "Forever";
                break;
        }

        List<TriviaScoresCountPoints> triviaScoresCountPoints = triviaScoresRepository.countTotalIdsByAnswerAndDates(fromDate, toDate);

        int numberToShow = Math.min(triviaScoresCountPoints.size(), 10); // This is just to know what to use the looping. If there are more than 10 rows returned from DB 10 should be max

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
            sb.append(triviaScoresCountPoints.get(i).getUserId());
            sb.append(" - ");
            sb.append(triviaScoresCountPoints.get(i).getPoints());
            sb.append("\n");
        }

        if (numberToShow == 0) {
            return event.reply("Det finns ingen ställning för den perioden än");
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(90, 130, 180))
                .title("Ställning " + nowShowing)
                .addField(EmbedCreateFields.Field.of("Användare - Poäng", sb.toString(), true))
                .build();

        return event.reply().withEmbeds(embed).then();
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

    enum scoresPeriod {
        CURRENT_MONTH,
        PREVIOUS_MONTH,
        ALL_TIME
    }
}
