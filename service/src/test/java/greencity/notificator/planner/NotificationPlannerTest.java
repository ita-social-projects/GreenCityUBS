package greencity.notificator.planner;

import greencity.dto.notification.ScheduledNotificationDto;
import greencity.enums.NotificationType;
import greencity.notificator.*;
import greencity.notificator.listener.NotificationPlanner;
import greencity.service.notificator.ScheduledNotificator;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.times;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static java.util.Collections.emptyList;

@ExtendWith(MockitoExtension.class)
class NotificationPlannerTest {
    private NotificationPlanner notificationPlanner;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    @ParameterizedTest
    @MethodSource("notificatorsProvider")
    void scheduleNotificationsTest(List<ScheduledNotificator> notificators) {
        notificationPlanner = new NotificationPlanner(notificators);
        notificators
            .forEach(notificator -> when(notificator.notifyBySchedule()).thenReturn(new ScheduledNotificationDto()));

        assertDoesNotThrow(() -> notificationPlanner.scheduleNotifications());

        notificators.forEach(notificator -> verify(notificator).notifyBySchedule());
    }

    private static Stream<List<ScheduledNotificator>> notificatorsProvider() {
        return Stream.of(
            asList(mock(UnpaidOrderNotificator.class), mock(UnpaidPackagesNotificator.class)),
            asList(mock(UnpaidOrderNotificator.class), mock(ChangedStatusNotificator.class),
                mock(AddedViolationNotificator.class)),
            emptyList());
    }

    @Test
    void restartNotificationsWithNotNullScheduledFeatureOfNotificator() {
        var scheduledNotification = new ScheduledNotificationDto(
            NotificationType.UNPAID_ORDER, scheduledFuture, UnpaidOrderNotificator.class);
        var unpaidOrderNotificator = mock(UnpaidOrderNotificator.class);
        notificationPlanner = new NotificationPlanner(Collections.singletonList(unpaidOrderNotificator));

        when(scheduledFuture.cancel(anyBoolean())).thenReturn(true);
        when(unpaidOrderNotificator.notifyBySchedule()).thenReturn(scheduledNotification);

        notificationPlanner.scheduleNotifications();
        assertDoesNotThrow(() -> notificationPlanner.restartNotificator(NotificationType.UNPAID_ORDER));

        verify(scheduledFuture).cancel(anyBoolean());
        verify(unpaidOrderNotificator, times(2)).notifyBySchedule();
    }

    @Test
    void restartNotificationsWithNullScheduledFeatureOfNotificator() {
        var scheduledNotification = new ScheduledNotificationDto(
            NotificationType.UNPAID_ORDER, null, UnpaidOrderNotificator.class);
        var unpaidOrderNotificator = mock(UnpaidOrderNotificator.class);
        notificationPlanner = new NotificationPlanner(Collections.singletonList(unpaidOrderNotificator));

        when(unpaidOrderNotificator.notifyBySchedule()).thenReturn(scheduledNotification);
        notificationPlanner.scheduleNotifications();

        assertDoesNotThrow(() -> notificationPlanner.restartNotificator(NotificationType.UNPAID_ORDER));

        verify(scheduledFuture, never()).cancel(anyBoolean());
        verify(unpaidOrderNotificator, times(2)).notifyBySchedule();
    }
}