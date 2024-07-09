package se.skaegg.discordbot.service;

import se.skaegg.discordbot.dto.TriviaScorersByDate;

import java.time.LocalDate;
import java.util.List;

public interface TriviaService {

    List<TriviaScorersByDate> getTriviaScorersPerDay(LocalDate from, LocalDate to);
}
