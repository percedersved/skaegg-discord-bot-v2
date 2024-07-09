package se.skaegg.discordbot.dto;


import java.time.LocalDate;
import java.util.List;

public class TriviaScorersByDate {

	LocalDate questionDate;
	List<String> scorers;

	public TriviaScorersByDate(LocalDate questionDate, List<String> scorers) {
		this.questionDate = questionDate;
		this.scorers = scorers;
	}


	public LocalDate getQuestionDate() {
		return questionDate;
	}

	public void setQuestionDate(LocalDate questionDate) {
		this.questionDate = questionDate;
	}

	public List<String> getScorers() {
		return scorers;
	}

	public void setScorers(List<String> scorers) {
		this.scorers = scorers;
	}
}
