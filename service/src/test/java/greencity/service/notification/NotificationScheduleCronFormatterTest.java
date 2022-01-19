package greencity.service.notification;

import greencity.ModelUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class NotificationScheduleCronFormatterTest {

    @InjectMocks
    private NotificationScheduleCronFormatter notificationScheduleCronFormatter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFormat() {
        assertEquals("0 0 18 * * ?", notificationScheduleCronFormatter
            .format(ModelUtils.NOTIFICATION_SCHEDULE));
    }
}
