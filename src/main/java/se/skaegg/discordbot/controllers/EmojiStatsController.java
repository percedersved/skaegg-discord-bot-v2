package se.skaegg.discordbot.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.jpa.EmojiStatsCount;
import se.skaegg.discordbot.jpa.EmojiStatsCountPerDay;
import se.skaegg.discordbot.jpa.EmojiStatsCountPerDay2;
import se.skaegg.discordbot.jpa.EmojiStatsEntity;
import se.skaegg.discordbot.jpa.EmojiStatsRepository;

@RestController
public class EmojiStatsController {

	EmojiStatsRepository emojiStatsRepository;

	public EmojiStatsController(EmojiStatsRepository emojiStatsRepository) {
		this.emojiStatsRepository = emojiStatsRepository;
	}


	@GetMapping(path = "/emojicount", produces = "application/json")
	public List<EmojiStatsCount> getAllEmojiCounts() {
		return emojiStatsRepository.countTotalPerEmoji();
	}

	@GetMapping(path = "/emojistats", produces = "application/json")
	public List<EmojiStatsEntity> getEmojiStatsBetweenDates(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		if (from != null && to != null) {
			return emojiStatsRepository.findAllByDateBetween(from, to);
		}
		return emojiStatsRepository.findAll();
	}

	@GetMapping(path = "/emojistatsday", produces = "application/json")
	public List<EmojiStatsCountPerDay2> getAllEmojisPerDay(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
//		if (from != null && to != null) {
//			return emojiStatsRepository.countTotalPerDayDateFilter(from, to);
//		}

		return emojiStatsRepository.countTotalPerDay2();
	}

}
