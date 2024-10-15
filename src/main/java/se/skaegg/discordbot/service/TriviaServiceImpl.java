package se.skaegg.discordbot.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import se.skaegg.discordbot.dto.TriviaQuestionDTO;
import se.skaegg.discordbot.dto.TriviaScorersByDate;
import se.skaegg.discordbot.entity.TriviaQuestions;
import se.skaegg.discordbot.repository.TriviaQuestionsRepository;
import se.skaegg.discordbot.repository.TriviaScoresRepository;

@Service
public class TriviaServiceImpl implements TriviaService {

    private static final Logger LOG = LoggerFactory.getLogger(TriviaServiceImpl.class);
    private final TriviaQuestionsRepository triviaQuestionsRepository;
    private final TriviaScoresRepository triviaScoresRepository;

    public TriviaServiceImpl(TriviaQuestionsRepository triviaQuestionsRepository, TriviaScoresRepository triviaScoresRepository) {
        this.triviaQuestionsRepository = triviaQuestionsRepository;
        this.triviaScoresRepository = triviaScoresRepository;
    }


    @Override
    @Cacheable("triviaServiceCache")
    public List<TriviaScorersByDate> getTriviaScorersPerDay(LocalDate from, LocalDate to) {
        LOG.debug("TriviaService.getTriviaScoresPerDay was called and cache is invalid. Will read from database");
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

    @Override
    public TriviaQuestionDTO getTriviaQuestion(LocalDate date) {
        TriviaQuestions question = triviaQuestionsRepository.findByQuestionDate(date);
        return new TriviaQuestionDTO(question.getId(),
                question.getQuestion(),
                question.getQuestionDate(),
                question.getCorrectAnswer(),
                question.getIncorrectAnswers(),
                question.getCategory(),
                question.getDifficulty(),
                question.getType());
    }
}
