package greencity.service.ubs;

import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderLockServiceImpl implements OrderLockService {
    private final OrderRepository orderRepository;
    private final EmployeeRepository employeeRepository;
    @Value("${order.lock.duration.minutes}")
    private int lockDurationMinutes;
    private static final String REMOVE_LOCK_MESSAGE = "Remove lock from order with id: {}";
    private static final String SET_LOCK_MESSAGE = "Set lock to order with id: {}";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public synchronized void lockOrder(Long orderId, Long employeeId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new NotFoundException(EMPLOYEE_NOT_FOUND + employeeId));
        if (!order.isBlocked()) {
            order.setBlocked(true);
            order.setBlockedByEmployee(employee);
            order.setBlockedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info(SET_LOCK_MESSAGE, order.getId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public synchronized void unlockOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        order.setBlocked(false);
        order.setBlockedByEmployee(null);
        order.setBlockedAt(null);
        orderRepository.save(order);
        log.info(REMOVE_LOCK_MESSAGE, order.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkLockOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(lockDurationMinutes);
        int unlocked = orderRepository.unlockExpiredOrders(expirationTime);

        if (unlocked > 0) {
            log.info("Unlocked {} expired orders (cutoff time: {})", unlocked, expirationTime);
        } else {
            log.debug("No expired orders to unlock (time: {})", expirationTime);
        }
    }
}
