package greencity.dto.notification;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
public class NotificationTemplateLocalizedDto {
    @NotNull
    private Long id;
    @NotEmpty
    private String notificationType;
    @NotNull
    private TitleDto title;
    @NotNull
    private BodyDto body;
    private String notificationReceiverType;
    @NotNull
    private NotificationScheduleDto schedule;
}
