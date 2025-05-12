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
    private String triggerDescriptionUk;
    @NotNull
    private String triggerDescriptionEn;
    @NotNull
    private NotificationTime time;
    @NotNull
    private String timeDescriptionUk;
    @NotNull
    private String timeDescriptionEn;
    private String schedule;
    @NotNull
    private String titleUk;
    @NotNull
    private String titleEn;
    @NotNull
    private NotificationStatus notificationStatus;
    @NotNull
    private String userCategoryDescriptionUk;
    @NotNull
    private String userCategoryDescriptionEn;
    @NotNull
    private boolean scheduleUpdateForbidden;
}
