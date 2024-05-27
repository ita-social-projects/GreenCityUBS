package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static greencity.enums.NotificationType.HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF;

@ExtendWith(MockitoExtension.class)
class HalfPaidBroughtByHimselfNotificatorTest {
    @InjectMocks
    private HalfPaidBroughtByHimselfNotificator notificator;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private NotificationTaskScheduler taskScheduler;

    @Mock
    private NotificationService notificationService;

    @Test
    void notifyByScheduleTest() {
        var schedule = "0 20 * * * *";
        var expectedDto = new ScheduledNotificationDto();
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF))
            .thenReturn(schedule);
        doNothing().when(notificationService).notifyAllHalfPaidOrdersWithStatusBroughtByHimself();
        when(taskScheduler.scheduleNotification(runnableCaptor.capture(),
            eq(schedule), eq(HALF_PAID_ORDER_WITH_STATUS_BROUGHT_BY_HIMSELF),
            eq(HalfPaidBroughtByHimselfNotificator.class)))
            .thenReturn(expectedDto);

        ScheduledNotificationDto result = notificator.notifyBySchedule();

        runnableCaptor.getValue().run();

        assertEquals(expectedDto, result);
        verify(notificationTemplateRepository).findScheduleOfActiveTemplateByType(any());
        verify(notificationService).notifyAllHalfPaidOrdersWithStatusBroughtByHimself();
        verify(taskScheduler).scheduleNotification(any(), anyString(), any(), any());
    }
}