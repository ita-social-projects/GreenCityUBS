package greencity.repository;

import greencity.entity.user.Location;
import greencity.entity.user.ubs.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
    /**
     * Method return of {@link OrderAddress} address for current order.
     *
     * @return {@link OrderAddress}.
     */
    @Query(value = "SELECT o.* FROM orders as o "
        + " JOIN ubs_user as ubs ON o.ubs_user_id = ubs.id "
        + " JOIN address as addr ON addr.id = ubs.address_id "
        + " WHERE o.id = :orderId", nativeQuery = true)
    OrderAddress getOrderAddressByOrderId(Long orderId);

    /**
     * Method checks if {@link OrderAddress} exists by {@link Location}.
     *
     * @return boolean.
     */
    boolean existsByLocation(Location location);
}