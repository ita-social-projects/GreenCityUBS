package greencity.config;

import greencity.service.notification.NotificationServiceImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private NotificationServiceImpl notificationService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNotifyUnpaidOrders() {
        doNothing().when(notificationService).notifyUnpaidOrders();
        notificationScheduler.notifyUnpaidOrders();
        verify(notificationService).notifyUnpaidOrders();
    }

    @Test
    public void testNotifyHalfPaidPackages() {
        doNothing().when(notificationService).notifyAllHalfPaidPackages();
        notificationScheduler.notifyHalfPaidPackages();
        verify(notificationService).notifyAllHalfPaidPackages();
    }

    @Test
    public void testNotifyInactiveAccount() {
        doNothing().when(notificationService).notifyInactiveAccounts();
        notificationScheduler.notifyInactiveAccount();
        verify(notificationService).notifyInactiveAccounts();
    }
}