package se.skaegg.discordbot.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import se.skaegg.discordbot.jpa.EmojiStatsCount;
import se.skaegg.discordbot.jpa.EmojiStatsRepository;

@RestController
public class EmojiStatsController {

	EmojiStatsRepository emojiStatsRepository;

	public EmojiStatsController(EmojiStatsRepository emojiStatsRepository) {
		this.emojiStatsRepository = emojiStatsRepository;
	}


	@GetMapping("/emojistats")
	public List<EmojiStatsCount> getAllEmojiStats() {
		return emojiStatsRepository.countTotalPerEmoji();
	}
}
