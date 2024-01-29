package se.skaegg.discordbot.jpa;

import java.time.LocalDate;
import java.util.Map;

public class EmojiStatsCountPerDay2 {
	LocalDate date;
	Map<String, Object> breakDown;

	public EmojiStatsCountPerDay2(LocalDate date, String name, long msgCount, long reactCount) {
		this.date = date;
		this.breakDown = Map.of(name, new UserStats(msgCount, reactCount));
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Map<String, Object> getBreakDown() {
		return breakDown;
	}

	public void setBreakDown(Map<String, Object> breakDown) {
		this.breakDown = breakDown;
	}

	static class UserStats {
		long msgCount;
		long reactCount;

		public UserStats(long msgCount, long reactCount) {
			this.msgCount = msgCount;
			this.reactCount = reactCount;
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

}
