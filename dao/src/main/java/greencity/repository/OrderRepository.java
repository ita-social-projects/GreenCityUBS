package greencity.repository;

import greencity.enums.OrderPaymentStatus;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * The method returns undelivered orders to group them.
     *
     * @return list of {@link Order}.
     */
    @Query("select o from Address a "
        + "inner join UBSuser u "
        + "on a.id = u.orderAddress.id "
        + "inner join Order o "
        + "on o.ubsUser.id = u.id "
        + "where o.orderPaymentStatus = 'PAID'"
        + "and a.coordinates.latitude  > :latitude - 0.000001 and a.coordinates.latitude  < :latitude + 0.000001 "
        + "and a.coordinates.longitude > :longitude - 0.000001 and a.coordinates.longitude < :longitude + 0.000001 ")
    List<Order> undeliveredOrdersGroupThem(@Param(value = "latitude") double latitude,
        @Param(value = "longitude") double longitude);

    /**
     * Method returns {@link Order} of undelivered orders.
     *
     * @return list of {@link Order}.
     */
    @Query("select o from Address a inner join UBSuser u on a.id = u.orderAddress.id "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'PAID' and a.coordinates is not null")
    List<Order> undeliveredAddresses();

    /**
     * Method returns {@link Optional Order}.
     *
     * @return list of {@link Order}.
     */
    @Query(value = "SELECT O.* FROM ORDERS O "
        + "JOIN ORDER_BAG_MAPPING OBM "
        + "ON O.ID = OBM.ORDER_ID "
        + "WHERE O.ID = :OrderId "
        + "ORDER BY BAG_ID", nativeQuery = true)
    Optional<Order> getOrderDetails(@Param(value = "OrderId") Long id);

    /**
     * Method return {@link List} of {@link Order} done by {@link User}.
     *
     * @return a {@link List} of {@link Order}
     */
    @Query(nativeQuery = true, value = "SELECT orders.* FROM orders "
        + "INNER JOIN ubs_user ON orders.ubs_user_id = ubs_user.id "
        + "INNER JOIN users ON ubs_user.users_id = users.id "
        + "WHERE users.uuid = :uuid")
    List<Order> getAllOrdersOfUser(@Param(value = "uuid") String uuid);

    /**
     * Method that returns all orders by its {@link OrderPaymentStatus}.
     */
    List<Order> findAllByOrderPaymentStatus(OrderPaymentStatus orderPaymentStatus);

    /**
     * Method sets employee_id and makes blocked_status 'true' for all not blocked
     * orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET EMPLOYEE_ID = :employee_id, BLOCKED = TRUE WHERE BLOCKED = FALSE",
        nativeQuery = true)
    void setBlockedEmployeeForAllOrders(@Param("employee_id") Long id);

    /**
     * Method unblocks all orders. Needs some improvement.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET BLOCKED = FALSE, EMPLOYEE_ID = NULL WHERE employee_id = :employee_id",
        nativeQuery = true)
    void unblockAllOrders(@Param("employee_id") Long employeeId);

    /**
     * Method gets user order by order id.
     *
     * @author Max Boiarchuk
     */
    Optional<Order> findUserById(@Param(value = "orderId") Long orderId);

    /**
     * Method for getting last order of user by user's uuid if such order exists.
     *
     * @param usersUuid - user's uuid
     * @return Optional of {@link Order}
     * @author Yurii Fedorko
     */
    @Query(nativeQuery = true,
        value = "SELECT o.* FROM orders o "
            + "INNER JOIN users u ON o.users_id = u.id "
            + "WHERE u.uuid = :user_uuid "
            + "ORDER BY o.order_date DESC "
            + "LIMIT 1")
    Optional<Order> getLastOrderOfUserByUUIDIfExists(@Param(value = "user_uuid") String usersUuid);

    /**
     * Method sets order status by order's id.
     *
     * @param orderId            - order's ID
     * @param orderPaymentStatus - order status to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders SET order_payment_status = :orderPaymentStatus WHERE id = :orderId")
    void updateOrderPaymentStatus(Long orderId, String orderPaymentStatus);

    /**
     * Return sum of discounts by order id.
     */
    @Query(nativeQuery = true,
        value = "select sum(COALESCE(c.points,0) + orders.points_to_use) from orders "
            + "left join certificate c on orders.id = c.order_id "
            + "where id= :orderId")
    Long findSumOfCertificatesByOrderId(Long orderId);

    /**
     * Method sets order points to use by order's id.
     *
     * @param orderId     - order's ID
     * @param pointsToUse - order points to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders SET points_to_use = :pointsToUse WHERE id = :orderId")
    void updateOrderPointsToUse(Long orderId, int pointsToUse);

    /**
     * Method update orders status from actual status to expected status by specific
     * date.
     *
     * @param actualStatus   - update from order status
     * @param expectedStatus - update to order status
     * @param currentDate    - order date of export
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders "
            + "SET order_status = :expected_status "
            + "WHERE order_status = :actual_status "
            + "AND date_of_export = :currentDate")
    void updateOrderStatusToExpected(@Param("actual_status") String actualStatus,
        @Param("expected_status") String expectedStatus,
        @Param("currentDate") LocalDate currentDate);

    /**
     * method returns all unpaid orders that contain a bag with id.
     */
    @Query(value = "select o from Order o "
        + "join fetch OrderBag obm on o.id = obm.order.id "
        + "join fetch Bag b on obm.bag.id = b.id "
        + "where obm.bag.id = :bagId and o.orderPaymentStatus = 'UNPAID'")
    List<Order> findAllUnpaidOrdersByBagId(Integer bagId);

    /**
     * method returns all unpaid orders that contain a bag with id.
     */
    @Query(value = "select o from Order o "
        + "join fetch o.ubsUser "
        + "join fetch OrderBag obm on o.id = obm.order.id "
        + "where obm.bag.id = :bagId and o.orderPaymentStatus = 'UNPAID'")
    List<Order> findAllUnpaidOrdersWithUsersByBagId(Integer bagId);

    /**
     * method returns all orders that contain a bag with id.
     */
    @Query(nativeQuery = true,
        value = "select o.* from orders o "
            + "left join order_bag_mapping obm on o.id = obm.order_id "
            + "where obm.bag_id = :bagId")
    List<Order> findAllByBagId(Integer bagId);

    /**
     * Method retrieves orders by order status and order payment status.
     *
     * @param orderStatus   - status of the order.
     * @param paymentStatus - payment status of the order.
     */
    List<Order> findAllByOrderStatusAndOrderPaymentStatus(OrderStatus orderStatus, OrderPaymentStatus paymentStatus);

    /**
     * Method retrieves orders by order status join events.
     *
     * @param orderStatus - status of the order.
     */
    @Query("select o from Order o "
        + "left join fetch o.events e WHERE o.orderStatus = :orderStatus")
    List<Order> findAllByOrderStatusWithEvents(@Param("orderStatus") OrderStatus orderStatus);

    /**
     * Method retrieves orders by event names join events.
     *
     * @param eventNames - names of events which are related to the order.
     */
    @Query("select o from Order o "
        + "inner join fetch o.events e WHERE e.eventName IN (:eventNames)")
    List<Order> findAllWithEventsByEventNames(@Param("eventNames") String... eventNames);

    /**
     * Method retrieves orders by order payment status join events.
     *
     * @param orderStatus - payment status of the orders.
     */
    @Query("select o from Order o "
        + "left join fetch o.events e WHERE o.orderPaymentStatus = :orderStatus")
    List<Order> findAllByOrderPaymentStatusWithEvents(@Param("orderStatus") OrderPaymentStatus orderStatus);

    /**
     * Method retrieves orders by order payment statuses and order statuses join
     * events.
     *
     * @param paymentStatuses - list of payment statues of the order.
     * @param orderStatuses   - list of order statues.
     */
    @Query("select o from Order o "
        + "inner join fetch o.events e "
        + "WHERE o.orderStatus in (:orderStatuses) AND o.orderPaymentStatus in (:paymentStatuses)")
    List<Order> findAllByPaymentStatusesAndOrderStatuses(
        @Param("paymentStatuses") List<OrderPaymentStatus> paymentStatuses,
        @Param("orderStatuses") List<OrderStatus> orderStatuses);
}