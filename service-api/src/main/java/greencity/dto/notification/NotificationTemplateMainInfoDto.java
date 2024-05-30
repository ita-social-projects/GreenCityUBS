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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateMainInfoDto {
    @NotEmpty
    private NotificationType type;
    @NotNull
    private NotificationTrigger trigger;
    @NotNull
    private String triggerDescription;
    @NotNull
    private String triggerDescriptionEng;
    @NotNull
    private NotificationTime time;
    @NotNull
    private String timeDescription;
    @NotNull
    private String timeDescriptionEng;
    private String schedule;
    @NotNull
    private String title;
    @NotNull
    private String titleEng;
    @NotNull
    private NotificationStatus notificationStatus;
    @NotNull
    private boolean scheduleUpdateForbidden;
}
