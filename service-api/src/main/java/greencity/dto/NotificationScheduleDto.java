package greencity.dto;

import greencity.entity.schedule.Schedule;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class NotificationScheduleDto extends Schedule {
}
