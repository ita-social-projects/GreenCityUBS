package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import java.util.concurrent.ScheduledFuture;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static greencity.enums.NotificationType.CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER;

@ExtendWith(MockitoExtension.class)
class CanceledViolationNotificatorTest {
    @InjectMocks
    private CanceledViolationNotificator notificator;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private NotificationTaskScheduler taskScheduler;

    @Mock
    private NotificationService notificationService;

    @Test
    @SuppressWarnings("unchecked")
    void notifyByScheduleTest() {
        var schedule = "0 20 * * * *";
        var expected =
            new ScheduledNotificationDto(CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER, notificator.getClass());
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(notificationTemplateRepository
            .findScheduleOfActiveTemplateByType(CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER)).thenReturn(schedule);
        doNothing().when(notificationService).notifyAllCanceledViolations();
        when(taskScheduler.scheduleNotification(runnableCaptor.capture(),
            eq(schedule), eq(CANCELED_VIOLATION_THE_RULES_BY_THE_MANAGER))).thenReturn(mock(ScheduledFuture.class));

        ScheduledNotificationDto result = notificator.notifyBySchedule();

        runnableCaptor.getValue().run();

        assertEquals(expected, result);
        verify(notificationTemplateRepository).findScheduleOfActiveTemplateByType(any());
        verify(notificationService).notifyAllCanceledViolations();
        verify(taskScheduler).scheduleNotification(any(), anyString(), any());
    }
}