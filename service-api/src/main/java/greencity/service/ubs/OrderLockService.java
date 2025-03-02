package greencity.service.ubs;

import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;

public interface OrderLockService {
    /**
     * Locks the order for editing by a specific employee. If the lock is not
     * released within the specified duration, it will be automatically unlocked.
     *
     * @param order    the order to lock.
     * @param employee the employee who locks the order.
     */
    void lockOrder(Order order, Employee employee);

    /**
     * Unlocks the order, allowing it to be edited again.
     *
     * @param order order to unlock.
     */
    void unlockOrder(Order order);

    /**
     * Periodically checks and unlocks orders that have been locked for longer than
     * the specified duration.
     */
    void checkLockOrders();
}
