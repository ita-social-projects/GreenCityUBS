package greencity.properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class OrderLockPropertiesTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private OrderLockProperties orderLockProperties;

    private LogCaptor logCaptor;

    @BeforeEach
    void setUp() {
        logCaptor = LogCaptor.forClass(OrderLockProperties.class);
        logCaptor.clearLogs();
    }

    @Test
    void getOrderLockDuration_shouldReturnValue_whenPropertyExists() {
        when(environment.getProperty("order.lock.duration.minutes", Integer.class))
            .thenReturn(15);

        int result = orderLockProperties.getOrderLockDuration();

        assertEquals(15, result);
        assertTrue(logCaptor.getErrorLogs().isEmpty());
    }

    @Test
    void getOrderLockDuration_shouldLogError_whenPropertyIsNull() {
        when(environment.getProperty("order.lock.duration.minutes", Integer.class))
            .thenReturn(null);

        int result = orderLockProperties.getOrderLockDuration();

        assertEquals(5, result);
        assertTrue(logCaptor.getErrorLogs().contains("OrderLockDuration property is empty"));
    }
}