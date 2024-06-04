package greencity.notificator.scheduler;

import greencity.enums.NotificationType;
import java.util.concurrent.ScheduledFuture;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
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
    @SuppressWarnings("unchecked")
    void scheduleNotificationWithCorrectCronExpressionTest() {
        var expected = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(CronTrigger.class)))
            .thenReturn(expected);

        var actual = notificationTaskScheduler.scheduleNotification(() -> {
        },
            "0 4 * * * *", NotificationType.COURIER_ITINERARY_FORMED);
        assertEquals(expected, actual);
        verify(taskScheduler).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    @ParameterizedTest
    @MethodSource(value = "incorrectCronExpressionsProvider")
    void scheduleNotificationWithIncorrectCronExpressionTest(String cronExpression) {
        var actual = notificationTaskScheduler
            .scheduleNotification(() -> {
            }, cronExpression, NotificationType.COURIER_ITINERARY_FORMED);

        assertNull(actual);
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(CronTrigger.class));
    }

    private static Stream<String> incorrectCronExpressionsProvider() {
        return Stream.of("", " ", null, "0 0 0 *", "* 1 * * *", "any text");
    }
}