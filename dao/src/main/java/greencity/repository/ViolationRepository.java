package greencity.repository;

import greencity.entity.user.Violation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViolationRepository extends PagingAndSortingRepository<Violation, Long> {
    /**
     * Method returns violation by order id.
     *
     * @param orderId {@link Long} .
     * @return optional of {@link Violation} .
     */
    Optional<Violation> findByOrderId(Long orderId);

    /**
     * Method gets all violations for user by id.
     *
     * @author Roman Sulymka
     */
    @Query(value = "select * from violations_description_mapping" +
        "    JOIN orders o on o.id = violations_description_mapping.order_id" +
        "WHERE users_id = :userId order by order_id asc", nativeQuery = true)
    Page<Violation> getAllViolationsByUserId(Pageable pageable, @Param(value = "userId") Long userId);

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
