package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    /**
     * Method returns {@link Coordinates} of undelivered orders.
     *
     * @return set of {@link Coordinates}.
     */
    @Query("select a.coordinates from Address a inner join UBSuser u on a.id = u.orderAddress.id "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderPaymentStatus = 'PAID' and a.coordinates is not null")
    Set<Coordinates> undeliveredOrdersCoords();

    /**
     * Method returns {@link Coordinates} of undelivered orders which not exceed
     * given capacity limit.
     *
     * @return set of {@link Coordinates}.
     */
    @Query("select a.coordinates "
        + "from UBSuser u "
        + "join Address a on a.id = u.orderAddress.id "
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
        + "join Address a on a.id = u.orderAddress.id "
        + "join Order o on u = o.ubsUser "
        + "join o.amountOfBagsOrdered bags "
        + "join Bag b on key(bags) = b.id "
        + "where o.orderPaymentStatus = 'PAID' "
        + "and a.coordinates.latitude  > :latitude - 0.000001 and a.coordinates.latitude  < :latitude + 0.000001 "
        + "and a.coordinates.longitude > :longitude - 0.000001 and a.coordinates.longitude < :longitude + 0.000001 ")
    int capacity(double latitude, double longitude);

    /**
     * Method returns list of not deleted {@link Address} addresses for current
     * user.
     *
     * @return list of {@link Address}.
     */
    @Query(value = "SELECT a.* FROM address a"
        + " WHERE user_id =:userId AND a.status != 'DELETED'", nativeQuery = true)
    List<Address> findAllNonDeletedAddressesByUserId(Long userId);

    /**
     * Finds the actual {@link Address} associated with the given user ID.
     *
     * @param userId the ID of the user whose address is being searched for
     * @return an {@link Optional} object containing the actual {@link Address}
     *         associated with the user, or an empty {@link Optional} if no such
     *         address is found
     */
    Optional<Address> findByUserIdAndActualTrue(Long userId);

    /**
     * Finds first non-deleted {@link Address} associated with the given user ID.
     *
     * @param userId the ID of the user whose address is being searched for
     * @return an {@link Optional} containing the first {@link Address} record that
     *         matches the provided userId and has an address status other than
     *         'DELETED', or an empty {@link Optional} if no matching record is
     *         found
     */
    @Query(value = "SELECT * FROM address WHERE user_id =:userId AND status != 'DELETED' LIMIT 1", nativeQuery = true)
    Optional<Address> findAnyByUserIdAndAddressStatusNotDeleted(Long userId);

    /**
     * Finds all addresses associated with a specific user.
     *
     * @param userId the ID of the user whose addresses are to be retrieved
     * @return a list of {@link Address} objects associated with the specified user
     */
    List<Address> findAllByUserId(Long userId);
}