package greencity.mapping.schedule;

import greencity.ModelUtils;
import greencity.dto.notification.NotificationScheduleDto;
import greencity.entity.schedule.NotificationSchedule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationScheduleDtoMapperTest {
    @InjectMocks
    private NotificationScheduleDtoMapper mapper;

    @Test
    void convert() {
        NotificationSchedule notificationSchedule = ModelUtils.getInfoAboutNotificationSchedule();
        NotificationScheduleDto dto1 = ModelUtils.getInfoAboutNotificationScheduleDto();
        Assertions.assertEquals(dto1, mapper.convert(notificationSchedule));
    }
}