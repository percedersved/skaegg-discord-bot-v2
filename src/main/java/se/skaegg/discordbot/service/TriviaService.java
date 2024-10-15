package se.skaegg.discordbot.service;

import java.time.LocalDate;
import java.util.List;

import se.skaegg.discordbot.dto.TriviaQuestionDTO;
import se.skaegg.discordbot.dto.TriviaScorersByDate;

public interface TriviaService {

    List<TriviaScorersByDate> getTriviaScorersPerDay(LocalDate from, LocalDate to);

	TriviaQuestionDTO getTriviaQuestion(LocalDate date);
}
