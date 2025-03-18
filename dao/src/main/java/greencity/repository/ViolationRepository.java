package greencity.repository;

import greencity.entity.user.Violation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ViolationRepository extends CrudRepository<Violation, Long> {
    /**
     * Method returns violation by order id.
     *
     * @param orderId {@link Long} .
     * @return optional of {@link Violation} .
     */
    Optional<Violation> findByOrderIdAndDescription(Long orderId, String description);

    /**
     * Method gets orderId for user by violationId.
     *
     * @author Roman Sulymka
     */
    @Query(value = "select order_id from violations_description_mapping"
        + " INNER JOIN orders o on o.id = violations_description_mapping.order_id"
        + " INNER JOIN users on o.users_id = users.id "
        + " WHERE violations_description_mapping.id = :violationId", nativeQuery = true)
    Long getOrderIdByViolationId(@Param(value = "violationId") Long violationId);

    /**
     * Method gets number of violations for user by userId.
     *
     * @author Roman Sulymka
     */
    @Query(value = "select count(vdm.id)"
        + "from violations_description_mapping vdm"
        + "    inner join orders o on o.id = vdm.order_id"
        + "    inner join users u on o.users_id = u.id"
        + "where u.id = :userId"
        + "  and vdm.violation_status = 'ACTIVE'", nativeQuery = true)
    Long getNumberOfViolationsByUser(@Param(value = "userId") Long userId);

    /**
     * Retrieves the active violation for the specified order ID.
     *
     * @param orderId the unique identifier of the order to find the active violation for
     * @return an Optional containing the active Violation if found, or an empty Optional if none exists
     */
    @Query(value = "SELECT v FROM Violation v WHERE v.order.id = ?1 AND v.violationStatus = 'ACTIVE'")
    Optional<Violation> findActiveViolationByOrderId(Long orderId);

    /**
     * Retrieves an optional canceled violation for the specified order ID.
     *
     * <p>This method uses a JPQL query to fetch the Violation associated with the given order ID
     * where the violation status is marked as 'DELETED', indicating a canceled violation.</p>
     *
     * @param orderId the identifier of the order to search for its canceled violation
     * @return an Optional containing the Violation if a matching canceled violation is found, or an empty Optional otherwise
     */
    @Query(value = "SELECT v FROM Violation v WHERE v.order.id = ?1 AND v.violationStatus = 'DELETED'")
    Optional<Violation> findCanceledViolationByOrderId(Long orderId);
}
