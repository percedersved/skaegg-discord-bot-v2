package se.skaegg.discordbot.configuration;

import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import se.skaegg.discordbot.handlers.Trivia;
import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

import java.time.LocalDate;

@Configuration
@EnableScheduling
public class DailyTriviaConfiguration {

    @Value("${trivia.daily.channelid}")
    String channelId;
    @Value("${trivia.url}")
    String url;
    @Value("${trivia.queryparams}")
    String queryParams;

    TriviaScoresRepository triviaScoresRepository;
    TriviaQuestionsRepository triviaQuestionsRepository;

    @Autowired
    GatewayDiscordClient client;

    public DailyTriviaConfiguration(TriviaQuestionsRepository triviaQuestionsRepository, TriviaScoresRepository triviaScoresRepository) {
        this.triviaQuestionsRepository = triviaQuestionsRepository;
        this.triviaScoresRepository = triviaScoresRepository;
    }

    @Scheduled(cron = "${trivia.cron.expression}")
    public void createDailyTriviaMessage() {

        Trivia trivia = new Trivia(triviaQuestionsRepository, triviaScoresRepository, client);
//        trivia.createQuestions(url, queryParams, channelId);
        trivia.createGetQuestionButton(channelId);
    }

    @Scheduled(cron = "${trivia.cron.dailypercentage}")
    public void createYesterdayTriviaCorrect() {
        Trivia trivia = new Trivia(triviaQuestionsRepository, triviaScoresRepository, client);
        trivia.displayCorrectAnswerPercentForDate(LocalDate.now().minusDays(1L), channelId);
    }


    // TODO: This is new. Trying to post last months results on the first day of the next month
//    @Scheduled(cron = "${trivia.cron.scorepost}")
//    public void postMonthlyResults() {
//        Trivia trivia = new Trivia(triviaQuestionsRepository, triviaScoresRepository, client);

//    }
}
