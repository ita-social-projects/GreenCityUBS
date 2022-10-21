package greencity.mapping.schedule;

import greencity.dto.notification.NotificationScheduleDto;
import greencity.entity.schedule.NotificationSchedule;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduleDtoMapper extends AbstractConverter<NotificationSchedule, NotificationScheduleDto> {
    @Override
    protected NotificationScheduleDto convert(NotificationSchedule notificationSchedule) {
        return NotificationScheduleDto.builder()
            .cron(notificationSchedule.getCron())
            .build();
    }
}
