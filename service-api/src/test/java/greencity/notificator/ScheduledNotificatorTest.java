package greencity.notificator;

import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ScheduledNotificatorTest {
    private final ScheduledNotificator notificator = spy(ScheduledNotificator.class);

    @Test
    void testClosePreviousTaskIfPresent() {
        ScheduledFuture<?> mockScheduledFuture = mock(ScheduledFuture.class);

        assertDoesNotThrow(() -> notificator.closePreviousTaskIfPresent(mockScheduledFuture));
        verify(mockScheduledFuture, times(1)).cancel(true);
    }

    @Test
    void testClosePreviousTaskIfPresentWithNull() {
        assertDoesNotThrow(() -> notificator.closePreviousTaskIfPresent(null));
        verifyNoInteractions(mock(ScheduledFuture.class));
    }
}
