package greencity.dto.telegram;

public record TelegramImageDto(Long messageId, String chatId, Boolean isRead, String fileUrl,
    String caption) implements TelegramMessageDto {
}
