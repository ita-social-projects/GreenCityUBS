package greencity.dto.telegram;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record EditTelegramMessageRequest(
        @NotNull Long chatId,
        @NotNull Integer messageId,
        @NotBlank @Size(max = 1000) String newText
) { }
