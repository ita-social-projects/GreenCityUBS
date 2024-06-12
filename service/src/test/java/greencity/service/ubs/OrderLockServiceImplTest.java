package greencity.service.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderLockServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderLockServiceImpl orderLockService;

    @Test
    void lockOrderTest() {
        Order order = new Order();
        order.setId(1L);
        Employee employee = new Employee();
        employee.setId(1L);

        LocalDateTime currentTime = LocalDateTime.now();

        orderLockService.lockOrder(order, employee);

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
            .blockedByEmployee(Employee.builder()
                .id(1L)
                .build())
            .blockedAt(LocalDateTime.now())
            .build();

        orderLockService.unlockOrder(order);

        verify(orderRepository, times(1)).save(order);

        assertFalse(order.isBlocked());
        assertNull(order.getBlockedByEmployee());
        assertNull(order.getBlockedAt());
    }
}
