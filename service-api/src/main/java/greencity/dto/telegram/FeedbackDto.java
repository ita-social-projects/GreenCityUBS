package greencity.dto.telegram;

public record FeedbackDto(Long id, String chatId, int rating, String comment) {
}
