package se.skaegg.discordbot.jpa;

import java.time.LocalDate;
import java.util.List;

public class EmojiStatsCountPerDay {
	private LocalDate date;
	private List<Usage> usage;

	public EmojiStatsCountPerDay(LocalDate date, List<Usage> usage) {
		this.date = date;
		this.usage = usage;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<Usage> getUsage() {
		return usage;
	}

	public void setUsage(List<Usage> usage) {
		this.usage = usage;
	}


	public record Usage(String emojiId, String name, long msgCount, long reactCount) {


	}
}
