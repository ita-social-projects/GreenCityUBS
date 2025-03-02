package greencity.notificator;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import io.grpc.netty.shaded.io.netty.util.concurrent.ScheduledFuture;
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
import static greencity.enums.NotificationType.COURIER_ITINERARY_FORMED;

@ExtendWith(MockitoExtension.class)
class CourierInternallyFormedNotificatorTest {
    @InjectMocks
    private CourierInternallyFormedNotificator notificator;

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
        var expectedDto = new ScheduledNotificationDto(COURIER_ITINERARY_FORMED, notificator.getClass());
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(notificationTemplateRepository.findScheduleOfActiveTemplateByType(COURIER_ITINERARY_FORMED))
            .thenReturn(schedule);
        doNothing().when(notificationService).notifyAllCourierItineraryFormed();
        when(taskScheduler.scheduleNotification(runnableCaptor.capture(),
            eq(schedule), eq(COURIER_ITINERARY_FORMED))).thenReturn(mock(ScheduledFuture.class));

        ScheduledNotificationDto result = notificator.notifyBySchedule();

        runnableCaptor.getValue().run();

        assertEquals(expectedDto, result);
        verify(notificationTemplateRepository).findScheduleOfActiveTemplateByType(any());
        verify(notificationService).notifyAllCourierItineraryFormed();
        verify(taskScheduler).scheduleNotification(any(), anyString(), any());
    }

}
