package se.skaegg.discordbot.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.dto.TriviaQuestionDTO;
import se.skaegg.discordbot.dto.TriviaScorersByDate;
import se.skaegg.discordbot.service.TriviaService;

@RestController
public class TriviaController {

	TriviaService triviaService;

	public TriviaController(TriviaService triviaService) {
		this.triviaService = triviaService;
	}


	@GetMapping(path = "/trivia-scorers", produces = "application/json")
	public List<TriviaScorersByDate> getTriviaScores(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
	                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		return triviaService.getTriviaScorersPerDay(from, to);
	}

	@GetMapping(path = "/trivia-question/{date}", produces = "application/json")
	public TriviaQuestionDTO getTriviaQuestion(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return triviaService.getTriviaQuestion(date);
	}
}
