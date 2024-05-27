package greencity.notificator.scheduler;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.enums.NotificationType;
import greencity.notificator.CourierInternallyFormedNotificator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationTaskSchedulerTest {
    @InjectMocks
    private NotificationTaskScheduler notificationTaskScheduler;

    @Mock
    private TaskScheduler taskScheduler;

    @Test
    void scheduleNotificationWithCorrectCronExpressionTest() {
        var expected = new ScheduledNotificationDto(
            NotificationType.COURIER_ITINERARY_FORMED, null, CourierInternallyFormedNotificator.class);

        when(taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class))).thenReturn(null);

        var actual = notificationTaskScheduler
            .scheduleNotification(() -> {
            }, "0 4 * * * *", NotificationType.COURIER_ITINERARY_FORMED,
                CourierInternallyFormedNotificator.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @ParameterizedTest
    @MethodSource(value = "incorrectCronExpressionsProvider")
    void scheduleNotificationWithIncorrectCronExpressionTest(String cronExpression) {
        var expected = new ScheduledNotificationDto(
            NotificationType.COURIER_ITINERARY_FORMED, null, CourierInternallyFormedNotificator.class);

        var actual = notificationTaskScheduler
            .scheduleNotification(() -> {
            }, cronExpression, NotificationType.COURIER_ITINERARY_FORMED,
                CourierInternallyFormedNotificator.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    private static Stream<String> incorrectCronExpressionsProvider() {
        return Stream.of("", " ", null, "0 0 0 *", "* 1 * * *", "any text");
    }
}