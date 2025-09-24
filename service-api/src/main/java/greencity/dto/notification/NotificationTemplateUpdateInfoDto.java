package greencity.dto.notification;

import greencity.annotations.ValidSchedulePattern;
import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
import greencity.enums.UserCategory;
import jakarta.validation.constraints.NotEmpty;
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
public class NotificationTemplateUpdateInfoDto {
    @NotNull
    private String titleUk;
    @NotNull
    private String titleEn;
    @NotNull
    private NotificationTrigger trigger;
    @NotEmpty
    private NotificationType type;
    @NotEmpty
    private UserCategory userCategory;
    @NotNull
    private NotificationTime time;
    @ValidSchedulePattern
    private String schedule;
}
