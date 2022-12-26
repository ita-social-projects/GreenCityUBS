package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    /**
     * Method returns {@link Coordinates} of undelivered orders.
     *
     * @return list of {@link Coordinates}.
     */
    @Query("select a.coordinates from Address a inner join UBSuser u on a.id = u.address.id "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderPaymentStatus = 'PAID' and a.coordinates is not null")
    Set<Coordinates> undeliveredOrdersCoords();

    /**
     * Method returns {@link Coordinates} of undelivered orders which not exceed
     * given capacity limit.
     *
     * @return list of {@link Coordinates}.
     */
    @Query("select a.coordinates "
        + "from UBSuser u "
        + "join Address a on a.id = u.address.id "
        + "join Order o on u = o.ubsUser "
        + "join o.amountOfBagsOrdered bags "
        + "join Bag b on key(bags) = b.id "
        + "where o.orderStatus = 'PAID' "
        + "and a.coordinates is not null "
        + "group by a.coordinates "
        + "having sum(bags*b.capacity) <= :maxCapacity")
    Set<Coordinates> undeliveredOrdersCoordsWithCapacityLimit(long maxCapacity);

    /**
     * Method returns amount of litres to be delivered in 1 or same address orders.
     *
     * @return {@link Integer}.
     */
    @Query("select sum(bags * b.capacity) "
        + "from UBSuser u "
        + "join Address a on a.id = u.address.id "
        + "join Order o on u = o.ubsUser "
        + "join o.amountOfBagsOrdered bags "
        + "join Bag b on key(bags) = b.id "
        + "where o.orderPaymentStatus = 'PAID' "
        + "and a.coordinates.latitude  > :latitude - 0.000001 and a.coordinates.latitude  < :latitude + 0.000001 "
        + "and a.coordinates.longitude > :longitude - 0.000001 and a.coordinates.longitude < :longitude + 0.000001 ")
    int capacity(double latitude, double longitude);

    /**
     * Method returns list of not deleted {@link Address}addresses for current user.
     *
     * @return list of {@link Address}.
     */
    @Query(value = "SELECT * FROM address a"
        + " WHERE user_id =:userId AND a.status != 'DELETED'", nativeQuery = true)
    List<Address> findAllNonDeletedAddressesByUserId(Long userId);

    /**
     * Method return of {@link Address}addresses for current order.
     *
     * @return {@link Address}.
     */
    @Query(value = "SELECT * FROM orders as o "
        + " JOIN ubs_user as ubs ON o.ubs_user_id = ubs.id "
        + " JOIN address as addr ON addr.id = ubs.address_id "
        + " WHERE o.id = :orderId", nativeQuery = true)
    Address getAddressByOrderId(Long orderId);
}