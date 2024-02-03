package se.skaegg.discordbot.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.skaegg.discordbot.jpa.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class EmojiStatsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmojiStatsController.class);
	EmojiStatsRepository emojiStatsRepository;
	List<EmojiStatsCountPerDayRaw> cachedAllEmojiStatsPerDayRaw;
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

		LOGGER.info("Received GET request for /emojistatsday");
		checkCache(); // Check cache. If cache is valid, nothing is done. Otherwise refresh cache
		List<EmojiStatsCountPerDay> result = filterAndGroupResult(from, to, count, offset);
		LOGGER.info("Finished processing GET request for /emojistatsday, now returning");
		return result;
	}

	private void checkCache() {
		if (cachedAllEmojiStatsPerDayRaw != null && lastEmojiStatsPerDayCacheTime != null &&
				lastEmojiStatsPerDayCacheTime.plusSeconds(900).isAfter(Instant.now())) {
			LOGGER.info("Cache for EmojiStatsPerDay is valid, no renewal");
			return;
		}
		// Refresh cache
		LOGGER.info("Cache for EmojiStatsPerDay is invalid, refreshing from database");
		cachedAllEmojiStatsPerDayRaw = emojiStatsRepository.countTotalPerDay();
		lastEmojiStatsPerDayCacheTime = Instant.now();
	}

	private List<EmojiStatsCountPerDay> filterAndGroupResult(LocalDate from, LocalDate to, int count, int offset) {

		List<EmojiStatsCountPerDayRaw> resultList;
		if (from != null && to != null) {
			// Filter the cached list to only include the correct dates
			resultList = cachedAllEmojiStatsPerDayRaw.stream()
					.filter(dbRow -> !(dbRow.getDate().isAfter(from) || dbRow.getDate().isBefore(to)))
					.toList();
		} else {
			// Return correct count of objects based on the count variable
			resultList = cachedAllEmojiStatsPerDayRaw.subList(offset, Math.min(offset + count, cachedAllEmojiStatsPerDayRaw.size()));
		}

		Map<LocalDate, List<EmojiStatsCountPerDay.Usage>> groupedResult = new LinkedHashMap<>();
		for (EmojiStatsCountPerDayRaw row : resultList) {
			EmojiStatsCountPerDay.Usage usage = new EmojiStatsCountPerDay.Usage(
					row.getEmojiId(),
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
		return result;
	}
}
