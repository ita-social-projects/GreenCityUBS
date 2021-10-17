package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
     * Finds list of Orders which the route and date garbage collection is defined.
     *
     * @return a {@link List} of {@link Order}.
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM orders o INNER JOIN  ubs_user u ON o.ubs_user_id = u.id "
            + "WHERE o.order_status LIKE 'ON_THE_ROUTE'")
    List<Order> getAllUsersInWhichTheRouteIsDefined();

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
     * Method return list of {@link Order} orders for user.
     *
     * @return {@link Order}.
     */

    @Query(value = "SELECT * FROM orders as o "
        + "JOIN ubs_user as ubs ON o.ubs_user_id = ubs.id "
        + "JOIN users as u ON ubs.users_id = u.id "
        + "WHERE u.uuid = :uuid", nativeQuery = true)
    List<Order> findAllOrdersByUserUuid(@Param("uuid") String uuid);

    /**
     * Method update status to 'ON_THE_ROUTE' for {@link Order} in which order
     * status is 'confirmed' and deliver from is current date.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET ORDER_STATUS = 'ON_THE_ROUTE' "
        + "WHERE ORDER_STATUS = 'CONFIRMED' AND DATE(DELIVER_FROM) <= CURRENT_DATE", nativeQuery = true)
    void updateOrderStatusToOnTheRoute();

    /**
     * Method that returns all orders by it's {@link OrderPaymentStatus}.
     */
    List<Order> findAllByOrderPaymentStatus(OrderPaymentStatus orderPaymentStatus);

    /**
     * Method change block status to 'true' for {@link Order} in order with certain
     * 'id'.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET BLOCKED = TRUE WHERE ID = :order_id", nativeQuery = true)
    void setOrderBlockedStatusBlocked(@Param("order_id") Long orderId);

    /**
     * Method change block status to 'false' for {@link Order} in order with certain
     * 'id'.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET BLOCKED = FALSE WHERE ID = :order_id", nativeQuery = true)
    void setOrderBlockedStatusUnblocked(@Param("order_id") Long orderId);

    /**
     * Method which is find eco number from shop.
     *
     * @param ecoNumber {@link String}.
     * @param orderId   {@link Long}.
     * @return {@link String}.
     * @author Yuriy Bahlay.
     */
    @Query(value = " SELECT additional_order FROM ORDER_ADDITIONAL "
        + "WHERE orders_id = :order_id AND additional_order =:eco_number ", nativeQuery = true)
    String findEcoNumberFromShop(@Param("eco_number") String ecoNumber, @Param("order_id") Long orderId);

    /**
     * This is method which is update eco number from shop.
     *
     * @param newEcoNumber {@link String}.
     * @param oldEcoNumber {@link String}.
     * @param orderId      {@link Long}.
     * @author Yuriy Bahlay.
     */
    @Modifying
    @Query(value = " UPDATE ORDER_ADDITIONAL SET additional_order =:new_eco_number "
        + " WHERE orders_id = :order_id AND additional_order =:old_eco_number", nativeQuery = true)
    void setOrderAdditionalNumber(@Param("new_eco_number") String newEcoNumber,
        @Param("old_eco_number") String oldEcoNumber,
        @Param("order_id") Long orderId);

    /**
     * Method changes order_status for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET ORDER_STATUS = :order_status WHERE BLOCKED = FALSE", nativeQuery = true)
    void changeStatusForAllOrders(@Param("order_status") String status);

    /**
     * Method changes date_of_export for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DATE_OF_EXPORT = :date_of_export WHERE BLOCKED = FALSE", nativeQuery = true)
    void changeDateOfExportForAllOrders(@Param("date_of_export")LocalDate date);

    /**
     * Method changes deliver_from for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DELIVER_FROM = :deliver_from WHERE BLOCKED = FALSE", nativeQuery = true)
    void changeDeliverFromForAllOrders(@Param("deliver_from")LocalDateTime time);

    /**
     * Method changes deliver_to for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET DELIVER_TO = :deliver_to WHERE BLOCKED = FALSE", nativeQuery = true)
    void changeDeliverToForAllOrders(@Param("deliver_to") LocalDateTime time);

    /**
     * Method changes receiving_station for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET RECEIVING_STATION = :receiving_station WHERE BLOCKED = FALSE", nativeQuery = true)
    void changeReceivingStationForAllOrders(@Param("receiving_station") String station);

    /**
     * Method sets employee_id and makes blocked_status 'true' for all not blocked orders.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET EMPLOYEE_ID = :employee_id, BLOCKED = TRUE WHERE BLOCKED = FALSE", nativeQuery = true)
    void setBlockedEmployeeForAllOrders(@Param("employee_id") Long id);

    /**
     * Method unblocks all orders.
     * Needs some improvement.
     */
    @Modifying
    @Query(value = "UPDATE ORDERS SET BLOCKED = FALSE WHERE BLOCKED = TRUE", nativeQuery = true)
    void unblockAllOrders();
}
