package greencity.repository;

import greencity.entity.enums.OrderPaymentStatus;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
