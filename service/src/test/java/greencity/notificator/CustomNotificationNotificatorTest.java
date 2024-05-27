package greencity.notificator;

import greencity.ModelUtils;
import greencity.dto.notification.ScheduledNotificationDto;
import greencity.entity.notifications.NotificationTemplate;
import greencity.notificator.scheduler.NotificationTaskScheduler;
import greencity.repository.NotificationTemplateRepository;
import greencity.service.ubs.NotificationService;
import io.grpc.netty.shaded.io.netty.util.concurrent.ScheduledFuture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static greencity.enums.NotificationType.CUSTOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomNotificationNotificatorTest {
    @InjectMocks
    private CustomNotificationsNotificator notificator;

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;

    @Mock
    private NotificationTaskScheduler taskScheduler;

    @Mock
    private NotificationService notificationService;

    @Test
    @SuppressWarnings("unchecked")
    void notifyByScheduleTest() {
        var templates = getTemplates();
        var expectedDto = new ScheduledNotificationDto(CUSTOM, notificator.getClass());
        var runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        when(notificationTemplateRepository.findAllActiveCustomNotificationsTemplates()).thenReturn(templates);
        doNothing().when(notificationService).notifyCustom(anyLong());
        when(taskScheduler.scheduleNotification(
            runnableCaptor.capture(),
            anyString(), any())).thenReturn(mock(ScheduledFuture.class));

        ScheduledNotificationDto result = notificator.notifyBySchedule();

        assertEquals(expectedDto, result);

        runnableCaptor.getAllValues().forEach(Runnable::run);
        verify(notificationTemplateRepository).findAllActiveCustomNotificationsTemplates();
        verify(notificationService, times(2)).notifyCustom(anyLong());
        verify(taskScheduler, times(2)).scheduleNotification(any(), anyString(), any());
    }

    private List<NotificationTemplate> getTemplates() {
        var template1 = ModelUtils.getCustomNotificationTemplate();
        var template2 = ModelUtils.getCustomNotificationTemplate();
        template2.setId(template1.getId() + 1);
        return List.of(template1, template2);
    }

}
