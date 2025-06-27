package greencity.dto.telegram;

public record TelegramImageDto(Long messageId, String chatId, String fileUrl,
    String caption, boolean isManagerPhoto) implements TelegramMessageDto {
}
