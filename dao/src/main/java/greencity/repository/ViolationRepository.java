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
    Optional<Violation> findByOrderId(Long orderId);

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
    @Query(value = "select count(violations_description_mapping.id)"
        + " from violations_description_mapping"
        + " INNER JOIN orders o on o.id = violations_description_mapping.order_id"
        + " INNER JOIN users on o.users_id = users.id "
        + " WHERE users_id = :userId", nativeQuery = true)
    Long getNumberOfViolationsByUser(@Param(value = "userId") Long userId);
}
