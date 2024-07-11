package se.skaegg.discordbot.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.skaegg.discordbot.dto.EmojiStatsCount;
import se.skaegg.discordbot.dto.EmojiStatsCountPerDay;
import se.skaegg.discordbot.entity.EmojiStats;
import se.skaegg.discordbot.service.EmojiService;

import java.time.LocalDate;
import java.util.List;

@RestController
public class EmojiController {

	EmojiService emojiService;

	public EmojiController(EmojiService emojiService) {
		this.emojiService = emojiService;
	}

	@GetMapping(path = "/emojicount", produces = "application/json")
	public List<EmojiStatsCount> getAllEmojiCounts() {
		return emojiService.getTotalCountPerEmoji();
	}

	@GetMapping(path = "/emojistats", produces = "application/json")
	public List<EmojiStats> getEmojiStatsBetweenDates(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return emojiService.getEmojiStats(from, to);
	}

	@GetMapping(path = "/emojistatsday", produces = "application/json")
	public List<EmojiStatsCountPerDay> getAllEmojisPerDay(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "30") int count, // This is number of days to get
			@RequestParam(required = false, defaultValue = "0") int offset) {

		return emojiService.getEmojisUsedPerDay(from, to, count, offset);
	}
}