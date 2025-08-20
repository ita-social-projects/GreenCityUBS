package greencity.dto.telegram;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record DeleteTelegramMessageRequest(
        @NotNull Long chatId,
        Integer messageId,
        Long assetId) { }
