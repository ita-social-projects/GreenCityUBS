package greencity.repository;

import greencity.entity.user.Location;
import greencity.entity.user.ubs.OrderAddress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
    /**
     * Method checks if {@link OrderAddress} exists by {@link Location}.
     *
     * @return boolean.
     */
    boolean existsByLocation(Location location);

    /**
     * Finds an {@link OrderAddress} by order ID.
     *
     * @param orderId the ID of the order whose address is being searched for
     * @return an {@link Optional} object containing the {@link OrderAddress}
     *         associated with the order, or an empty {@link Optional} if no such
     *         address is found
     */
    @Query(value = """
        SELECT a.*
        FROM orders o
        left join ubs_user uu on o.ubs_user_id = uu.id
        left join order_address a on uu.id = a.id
        where o.id = :orderId
        """,
        nativeQuery = true)
    Optional<OrderAddress> findByOrderId(Long orderId);
}