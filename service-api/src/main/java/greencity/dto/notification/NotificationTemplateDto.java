package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDto {
    @NotNull
    private Long id;

    @NotNull
    private NotificationTemplateMainInfoDto notificationTemplateMainInfoDto;
}
