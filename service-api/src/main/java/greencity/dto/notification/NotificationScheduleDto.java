package greencity.dto.notification;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class NotificationScheduleDto {
    @NotEmpty
    private String cron;
}
