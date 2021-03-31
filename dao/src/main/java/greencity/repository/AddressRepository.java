package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    /**
     * Method returns {@link Coordinates} of undelivered orders.
     *
     * @return list of {@link Coordinates}.
     */
    @Query("select a.coordinates from Address a inner join UBSuser u on a = u.userAddress "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'PAID' and a.coordinates is not null")
    Set<Coordinates> undeliveredOrdersCoords();

    /**
     * Method returns amount of litres to be delivered in 1 or same address orders.
     *
     * @return {@link Integer}.
     */
    @Query(nativeQuery = true, value = "select sum(amount*capacity) "
        + "from address "
        + "join ubs_user "
        + "on address.id = ubs_user.address_id "
        + "join orders "
        + "on ubs_user.id = orders.ubs_user_id "
        + "join order_bag_mapping "
        + "on orders.id = order_bag_mapping.order_id "
        + "join bag "
        + "on order_bag_mapping.bag_id = bag.id "
        + "where latitude = :latitude and longitude = :longitude and order_status = 'PAID';")
    int capacity(double latitude, double longitude);
}
