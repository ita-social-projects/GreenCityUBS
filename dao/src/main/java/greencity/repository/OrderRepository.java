package greencity.repository;

import greencity.entity.order.Order;
import greencity.entity.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, Long> {
    /**
     * The method returns undelivered orders to group them.
     *
     * @return list of {@link Order}.
     */
    @Query(nativeQuery = true, value = "select * "
        + "from address "
        + "inner join ubs_user "
        + "on address.id = ubs_user.address_id "
        + "inner join orders "
        + "on orders.ubs_user_id = ubs_user.id "
        + "where orders.order_status = 'PAID'"
        + "and address.latitude = :latitude "
        + "and address.longitude = :longitude")
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
    @Query(value = "SELECT * FROM ORDERS AS O "
        + "JOIN ORDER_BAG_MAPPING AS OBM "
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
}
