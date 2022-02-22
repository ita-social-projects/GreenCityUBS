package greencity.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Builder
@Getter
public class UpdateNotificationTemplatesDto {
    @NotNull
    String body;
    @NotNull
    String notificationType;
    @NotNull
    long languageId;
}
