package greencity.dto.notification;

import greencity.enums.NotificationTime;
import greencity.enums.NotificationTrigger;
import greencity.enums.NotificationType;
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
    private String title;
    @NotNull
    private String titleEng;
    @NotNull
    private NotificationTrigger trigger;
    @NotEmpty
    private NotificationType type;
    @NotNull
    private NotificationTime time;
    private String schedule;
}
