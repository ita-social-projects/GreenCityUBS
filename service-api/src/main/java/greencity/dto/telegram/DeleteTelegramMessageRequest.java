package greencity.dto.telegram;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record DeleteTelegramMessageRequest(
        @NotNull Long chatId,
        @NotNull Integer messageId,
        Long assetId) { }
