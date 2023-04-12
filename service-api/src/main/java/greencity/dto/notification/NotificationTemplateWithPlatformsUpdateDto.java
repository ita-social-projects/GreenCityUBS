package greencity.dto.notification;

import greencity.enums.NotificationStatus;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateWithPlatformsUpdateDto {
    @NotNull
    private NotificationTemplateMainInfoDto notificationTemplateMainInfoDto;
    @NotNull
    private List<NotificationPlatformDto> platforms;
}
