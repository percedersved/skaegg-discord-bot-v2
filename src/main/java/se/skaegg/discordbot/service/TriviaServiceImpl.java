package se.skaegg.discordbot.service;

import org.springframework.stereotype.Service;
import se.skaegg.discordbot.dto.TriviaScorersByDate;
import se.skaegg.discordbot.repository.TriviaQuestionsRepository;
import se.skaegg.discordbot.repository.TriviaScoresRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TriviaServiceImpl implements TriviaService {

    TriviaQuestionsRepository triviaQuestionsRepository;
    TriviaScoresRepository triviaScoresRepository;

    public TriviaServiceImpl(TriviaQuestionsRepository triviaQuestionsRepository, TriviaScoresRepository triviaScoresRepository) {
        this.triviaQuestionsRepository = triviaQuestionsRepository;
        this.triviaScoresRepository = triviaScoresRepository;
    }


    @Override
    public List<TriviaScorersByDate> getTriviaScorersPerDay(LocalDate from, LocalDate to) {
        List<LocalDate> dates;
        if (from != null && to != null) {
            dates = triviaQuestionsRepository.findAllDistinctQuestionDatesBetween(from, to);
        }
        else {
            dates = triviaQuestionsRepository.findAllDistinctQuestionDates();
        }

        return dates.stream().map(date -> {
                    List<String> scorers = triviaScoresRepository.correctScoresPerQuestionDate(date);
                    if (!scorers.isEmpty()) {
                        return new TriviaScorersByDate(date, scorers);
                    }
                    else {
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Remove nulls
                .toList();
    }
}
