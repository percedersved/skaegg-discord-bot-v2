package se.skaegg.discordbot.dto;

import java.time.LocalDate;
import java.util.List;

public record TriviaQuestionDTO(Integer id,
                                String question,
                                LocalDate questionDate,
                                String correctAnswer,
                                List<String> incorrectAnswers,
                                String category,
                                String difficulty,
                                String type) {
}
