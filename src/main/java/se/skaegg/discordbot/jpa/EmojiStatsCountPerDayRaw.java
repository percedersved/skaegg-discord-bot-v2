package se.skaegg.discordbot.jpa;

import java.time.LocalDate;

/**
 * This class is used to store the raw data from the database query.
 * It is then used to create the EmojiStatsCountPerDay object.
 */

public class EmojiStatsCountPerDayRaw {

	LocalDate date;
	String name;
	long msgCount;
	long reactCount;


	public EmojiStatsCountPerDayRaw(LocalDate date, String name, long msgCount, long reactCount) {
		this.date = date;
		this.name = name;
		this.msgCount = msgCount;
		this.reactCount = reactCount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(long msgCount) {
		this.msgCount = msgCount;
	}

	public long getReactCount() {
		return reactCount;
	}

	public void setReactCount(long reactCount) {
		this.reactCount = reactCount;
	}
}
