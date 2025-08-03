package se.skaegg.discordbot.configuration;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import se.skaegg.discordbot.handler.Trivia;
import se.skaegg.discordbot.repository.TriviaButtonClicksRepository;
import se.skaegg.discordbot.repository.TriviaQuestionsRepository;
import se.skaegg.discordbot.repository.TriviaScoresRepository;

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
    TriviaButtonClicksRepository triviaButtonClicksRepository;
    Trivia trivia;

    public DailyTriviaConfiguration(Trivia trivia) {
        this.trivia = trivia;
    }



    @Scheduled(cron = "${trivia.cron.expression}")
    public void createDailyTriviaMessage() {
        trivia.createGetQuestionButton(channelId);
    }

    @Scheduled(cron = "${trivia.cron.dailypercentage}")
    public void createYesterdayTriviaCorrect() {
        trivia.displayCorrectAnswerPercentForDate(LocalDate.now().minusDays(1L), channelId);
    }

    @Scheduled(cron = "${trivia.cron.monthlywinner}")
    public void createMonthlyTriviaWinner() {
        trivia.displayMonthlyWinner(channelId);
    }
}
