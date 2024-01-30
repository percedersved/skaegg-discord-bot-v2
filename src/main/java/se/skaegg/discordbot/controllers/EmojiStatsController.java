package se.skaegg.discordbot.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.jpa.EmojiStatsCount;
import se.skaegg.discordbot.jpa.EmojiStatsCountPerDay;
import se.skaegg.discordbot.jpa.EmojiStatsCountPerDayRaw;
import se.skaegg.discordbot.jpa.EmojiStatsEntity;
import se.skaegg.discordbot.jpa.EmojiStatsRepository;

@RestController
public class EmojiStatsController {

	EmojiStatsRepository emojiStatsRepository;
	List<EmojiStatsCountPerDay> cachedEmojiStatsPerDay;
	Instant lastEmojiStatsPerDayCacheTime;

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
	public List<EmojiStatsCountPerDay> getAllEmojisPerDay(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false, defaultValue = "30") int count, // This is number of days to get
			@RequestParam(required = false, defaultValue = "0") int offset) {

		return getCachedEmojisPerDay(from, to, count, offset);
	}

	private List<EmojiStatsCountPerDay> getCachedEmojisPerDay(LocalDate from, LocalDate to, int count, int offset) {

		// If the cache is valid, return the cached data (cache is valid for 15 minutes)
		if (cachedEmojiStatsPerDay != null && lastEmojiStatsPerDayCacheTime != null &&
				lastEmojiStatsPerDayCacheTime.plusSeconds(900).isAfter(Instant.now())) {
			return cachedEmojiStatsPerDay.subList(offset, Math.min(offset + count, cachedEmojiStatsPerDay.size()));
		}

		// If the cache is invalid, get the data from the database and renew the cache
		List<EmojiStatsCountPerDayRaw> dbResultRaw;
		if (from != null && to != null) {
			dbResultRaw = emojiStatsRepository.countTotalPerDayDateFilter(from, to);
		}
		else {
			dbResultRaw = emojiStatsRepository.countTotalPerDay();
		}

		Map<LocalDate, List<EmojiStatsCountPerDay.Usage>> groupedResult = new LinkedHashMap<>();
		for (EmojiStatsCountPerDayRaw row : dbResultRaw) {
			EmojiStatsCountPerDay.Usage usage = new EmojiStatsCountPerDay.Usage(
					row.getName(),
					row.getMsgCount(),
					row.getReactCount());

			// If the date is not in the map, add it with the usage data
			groupedResult.computeIfAbsent(row.getDate(), k -> new ArrayList<>()).add(usage);
		}

		// Convert the map to a list of EmojiStatsCountPerDay
		List<EmojiStatsCountPerDay> result = new ArrayList<>();
		for (Map.Entry<LocalDate, List<EmojiStatsCountPerDay.Usage>> entry : groupedResult.entrySet()) {
			EmojiStatsCountPerDay stats = new EmojiStatsCountPerDay(entry.getKey(), entry.getValue());
			result.add(stats);
		}

		cachedEmojiStatsPerDay = result;
		lastEmojiStatsPerDayCacheTime = Instant.now();
		return result.subList(offset, Math.min(offset + count, result.size()));
	}

}
