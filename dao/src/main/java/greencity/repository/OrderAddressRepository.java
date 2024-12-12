package greencity.repository;

import greencity.entity.user.ubs.OrderAddress;
import java.util.Optional;
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
    @Query(value = "SELECT * FROM orders as o "
        + " JOIN ubs_user as ubs ON o.ubs_user_id = ubs.id "
        + " JOIN address as addr ON addr.id = ubs.address_id "
        + " WHERE o.id = :orderId", nativeQuery = true)
    OrderAddress getOrderAddressByOrderId(Long orderId);

    /**
     * Finds an {@link OrderAddress} by order ID.
     *
     * @param orderId the ID of the order whose address is being searched for
     * @return an {@link Optional} object containing the {@link OrderAddress}
     *         associated with the order, or an empty {@link Optional} if no such
     *         address is found
     */
    @Query(value = "SELECT a.* FROM orders o "
        + "left join ubs_user uu on o.ubs_user_id = uu.id "
        + "left join order_address a on uu.id = a.id "
        + "where o.id = :orderId",
        nativeQuery = true)
    Optional<OrderAddress> findByOrderId(Long orderId);
}