package se.skaegg.discordbot.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.jpa.TriviaQuestionsRepository;
import se.skaegg.discordbot.jpa.TriviaScorersByDate;
import se.skaegg.discordbot.jpa.TriviaScoresRepository;

@RestController()
public class TriviaStatsController {

	TriviaQuestionsRepository triviaQuestionsRepository;
	TriviaScoresRepository triviaScoresRepository;

	public TriviaStatsController(TriviaQuestionsRepository triviaQuestionsRepository, TriviaScoresRepository triviaScoresRepository) {
		this.triviaQuestionsRepository = triviaQuestionsRepository;
		this.triviaScoresRepository = triviaScoresRepository;
	}

	@GetMapping(path = "/trivia-scorers", produces = "application/json")
	public List<TriviaScorersByDate> getTriviaScores(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

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
