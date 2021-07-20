package greencity.repository;

import greencity.entity.user.Violation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ViolationRepository extends CrudRepository<Violation, Long> {
    /**
     * Method returns violation by order id.
     *
     * @param orderId {@link Long} .
     * @return optional of {@link Violation} .
     */
    Optional<Violation> findByOrderId(Long orderId);
}
