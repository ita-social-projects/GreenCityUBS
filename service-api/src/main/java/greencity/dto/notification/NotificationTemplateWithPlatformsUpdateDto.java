package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateWithPlatformsUpdateDto {
    @NotNull
    private NotificationTemplateUpdateInfoDto notificationTemplateUpdateInfo;
    @NotNull
    private List<NotificationPlatformDto> platforms;
}
