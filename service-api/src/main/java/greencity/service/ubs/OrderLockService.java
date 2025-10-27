package greencity.service.ubs;

public interface OrderLockService {
    /**
     * Locks the order for editing by a specific employee. If the lock is not
     * released within the specified duration, it will be automatically unlocked.
     *
     * @param orderId    the order to lock.
     * @param employeeId the employee who locks the order.
     */
    void lockOrder(Long orderId, Long employeeId);

    /**
     * Unlocks the order, allowing it to be edited again.
     *
     * @param orderId order to unlock.
     */
    void unlockOrder(Long orderId);

    /**
     * Periodically checks and unlocks orders that have been locked for longer than
     * the specified duration.
     */
    void checkLockOrders();
}
