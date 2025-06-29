package greencity.dto.telegram;

import java.time.LocalDateTime;

public record TelegramImageDto(
        Long messageId,
        String chatId,
        LocalDateTime sendAt,
        String fileUrl,
        String caption,
        boolean isManagerPhoto
) implements TelegramMessageDto { }
