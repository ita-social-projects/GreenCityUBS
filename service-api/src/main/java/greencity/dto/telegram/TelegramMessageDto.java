package greencity.dto.telegram;

import java.time.LocalDateTime;

public interface TelegramMessageDto {
    String chatId();
    LocalDateTime sendAt();
}
