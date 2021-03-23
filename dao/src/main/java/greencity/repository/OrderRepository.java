package greencity.repository;

import greencity.entity.order.Order;
import java.util.List;

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
    @Query("select o from Address a inner join UBSuser u on a = u.userAddress "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'PAID' and a.coordinates is not null")
    List<Order> undeliveredAddresses();
}
