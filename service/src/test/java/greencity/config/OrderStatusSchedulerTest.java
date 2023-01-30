package greencity.config;

import greencity.service.ubs.UBSManagementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

    @InjectMocks
    OrderStatusScheduler orderStatusScheduler;

    @Mock
    UBSManagementServiceImpl ubsManagementService;

    @Test
    void autoChangeOrderStatusOnTheDayOfExportTest() {
        orderStatusScheduler.autoChangeOrderStatusOnTheDayOfExport();
        verify(ubsManagementService).updateOrderStatusOnTheDayOfExport();
    }
}
