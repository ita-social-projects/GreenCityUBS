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
import static greencity.enums.NotificationType.DONE_OR_CANCELED_UNPAID_ORDER;

@ExtendWith(MockitoExtension.class)
class DoneOrCanceledUnpaidOrderNotificatorTest {
    @InjectMocks
    private DoneOrCanceledUnpaidOrderNotificator notificator;

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

        when(notificationTemplateRepository.findScheduleOfActiveTemplateByType(DONE_OR_CANCELED_UNPAID_ORDER))
            .thenReturn(schedule);
        doNothing().when(notificationService).notifyAllDoneOrCanceledUnpaidOrders();
        when(taskScheduler.scheduleNotification(runnableCaptor.capture(),
            eq(schedule), eq(DONE_OR_CANCELED_UNPAID_ORDER), eq(DoneOrCanceledUnpaidOrderNotificator.class)))
            .thenReturn(expectedDto);

        ScheduledNotificationDto result = notificator.notifyBySchedule();

        runnableCaptor.getValue().run();

        assertEquals(expectedDto, result);
        verify(notificationTemplateRepository).findScheduleOfActiveTemplateByType(any());
        verify(notificationService).notifyAllDoneOrCanceledUnpaidOrders();
        verify(taskScheduler).scheduleNotification(any(), anyString(), any(), any());
    }
}