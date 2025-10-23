package greencity.service.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLockServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private OrderLockServiceImpl orderLockService;

    @Test
    void lockOrderTest() {
        Order order = new Order();
        order.setId(1L);
        Employee employee = new Employee();
        employee.setId(1L);
        LocalDateTime currentTime = LocalDateTime.now();

        when(orderRepository.findById(order.getId()))
            .thenReturn(Optional.of(order));
        when(employeeRepository.findById(employee.getId()))
            .thenReturn(Optional.of(employee));

        orderLockService.lockOrder(order.getId(), employee.getId());

        verify(orderRepository, times(1)).save(order);

        assertTrue(order.isBlocked());
        assertEquals(employee, order.getBlockedByEmployee());
        assertTrue(order.getBlockedAt().isAfter(currentTime.minusSeconds(1)));
    }

    @Test
    void unlockOrderTest() {
        Order order = Order.builder()
            .id(1L)
            .blocked(true)
            .blockedAt(LocalDateTime.now())
            .build();

        when(orderRepository.findById(order.getId()))
            .thenReturn(Optional.of(order));

        orderLockService.unlockOrder(order.getId());

        verify(orderRepository, times(1)).save(order);

        assertFalse(order.isBlocked());
        assertNull(order.getBlockedByEmployee());
        assertNull(order.getBlockedAt());
    }

    @Test
    void testCheckLockOrders() {
        when(orderRepository.unlockExpiredOrders(any(LocalDateTime.class))).thenReturn(2);
        orderLockService.checkLockOrders();
        verify(orderRepository, times(1)).unlockExpiredOrders(any(LocalDateTime.class));
    }

    @Test
void testCheckLockOrders_noExpired() {
        when(orderRepository.unlockExpiredOrders(any(LocalDateTime.class))).thenReturn(0);
        orderLockService.checkLockOrders();
        verify(orderRepository, times(1)).unlockExpiredOrders(any(LocalDateTime.class));
        }
}
