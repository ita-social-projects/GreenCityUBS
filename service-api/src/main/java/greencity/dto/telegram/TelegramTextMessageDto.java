package greencity.dto.telegram;

import java.time.LocalDateTime;

public record TelegramTextMessageDto(Long messageId, String chatId, Boolean isRead,
    LocalDateTime sendAt, String text, boolean isManagerMessage) implements TelegramMessageDto {
}
