package greencity.config;

import greencity.service.ubs.UBSManagementServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {
    @InjectMocks
    private OrderStatusScheduler orderStatusScheduler;

    @Mock
    private UBSManagementServiceImpl ubsManagementService;

    @Test
    void autoChangeOrderStatusOnTheDayOfExportTest() {
        orderStatusScheduler.autoUpdateOrderStatus();
        log.info("Update order status from actual status to expected status by the day of export");
        verify(ubsManagementService, times(1)).updateOrderStatusToExpected();
    }

}