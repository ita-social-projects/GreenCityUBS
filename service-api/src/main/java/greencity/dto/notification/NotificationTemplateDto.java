package greencity.dto.notification;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
