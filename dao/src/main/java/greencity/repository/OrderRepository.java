package greencity.repository;

import greencity.enums.OrderPaymentStatus;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        + "on a.id = u.address.id "
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
    @Query("select o from Address a inner join UBSuser u on a.id = u.address.id "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'PAID' and a.coordinates is not null")
    List<Order> undeliveredAddresses();

    /**
     * Method returns {@link Optional Order}.
     *
     * @return list of {@link Order}.
     */
    @Query(value = "SELECT * FROM ORDERS O "
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
    @Query(nativeQuery = true, value = "SELECT * FROM orders "
        + "INNER JOIN ubs_user ON orders.ubs_user_id = ubs_user.id "
        + "INNER JOIN users ON ubs_user.users_id = users.id "
        + "WHERE users.uuid = :uuid")
    List<Order> getAllOrdersOfUser(@Param(value = "uuid") String uuid);

    /**
     * Method that returns all orders by it's {@link OrderPaymentStatus}.
     */
    List<Order> findAllByOrderPaymentStatus(OrderPaymentStatus orderPaymentStatus);

    /**
     * Method changes order_status for all not blocked orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET ORDER_STATUS = :order_status WHERE employee_id = :employee_id",
        nativeQuery = true)
    void changeStatusForAllOrders(@Param("order_status") String status, @Param("employee_id") Long employeeId);

    /**
     * Method changes date_of_export for all not blocked orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DATE_OF_EXPORT = :date_of_export WHERE employee_id = :employee_id",
        nativeQuery = true)
    void changeDateOfExportForAllOrders(@Param("date_of_export") LocalDate date, @Param("employee_id") Long employeeId);

    /**
     * Method changes deliver_from for all not blocked orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DELIVER_FROM = :deliver_from WHERE employee_id = :employee_id",
        nativeQuery = true)
    void changeDeliverFromForAllOrders(@Param("deliver_from") LocalDateTime time,
        @Param("employee_id") Long employeeId);

    /**
     * Method changes deliver_to for all not blocked orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DELIVER_TO = :deliver_to WHERE employee_id = :employee_id", nativeQuery = true)
    void changeDeliverToForAllOrders(@Param("deliver_to") LocalDateTime time, @Param("employee_id") Long employeeId);

    /**
     * Method changes receiving_station for all not blocked orders.
     *
     * @author Liubomyr Pater.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET RECEIVING_STATION_ID = :receiving_station WHERE employee_id = :employee_id",
        nativeQuery = true)
    void changeReceivingStationForAllOrders(@Param("receiving_station") Long stationId,
        @Param("employee_id") Long employeeId);

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
        value = "SELECT * FROM orders o "
            + "INNER JOIN users u ON o.users_id = u.id "
            + "WHERE u.uuid = :user_uuid "
            + "ORDER BY o.order_date DESC "
            + "LIMIT 1")
    Optional<Order> getLastOrderOfUserByUUIDIfExists(@Param(value = "user_uuid") String usersUuid);

    /**
     * Method sets order status by order's id.
     *
     * @param orderId     - order's ID
     * @param orderStatus - order status to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders SET order_payment_status = :orderStatus WHERE id = :orderId")
    void updateOrderPaymentStatus(@Param(value = "orderId") Long orderId, @Param("orderStatus") String orderStatus);

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
     * Method sets order cancellation comment by order's id.
     *
     * @param orderId             - order's ID
     * @param cancellationComment - order cancellation comment to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders SET cancellation_comment = :cancellationComment WHERE id = :orderId")
    void updateCancelingComment(Long orderId, String cancellationComment);

    /**
     * Method sets admin comment for order by order id.
     * 
     * @param orderId      - order's ID
     * @param adminComment - admin comment to set
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE Order o SET o.adminComment =:adminComment WHERE o.id =:orderId")
    void updateAdminComment(Long orderId, String adminComment);

    /**
     * Method sets order cancellation reason by order's id.
     *
     * @param orderId            - order's ID
     * @param cancellationReason - order cancellation reason to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE orders SET cancellation_reason = :cancellationReason WHERE id = :orderId")
    void updateCancelingReason(Long orderId, String cancellationReason);

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
}
