package greencity.service.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
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
    @Value("${order.lock.duration.minutes}")
    private int lockDurationMinutes;
    private static final String REMOVE_LOCK_MESSAGE = "Remove lock from order with id: {}";
    private static final String SET_LOCK_MESSAGE = "Set lock to order with id: {}";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public synchronized void lockOrder(Order order, Employee employee) {
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
    public synchronized void unlockOrder(Order order) {
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
        orderRepository.unlockExpiredOrders(expirationTime);
        log.info("Unlock orders");
    }
}
