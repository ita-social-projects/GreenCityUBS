package greencity.dto.notification;

import lombok.*;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateNotificationTemplatesDto {
    @NotNull
    String body;
    @NotNull
    String notificationType;
}
