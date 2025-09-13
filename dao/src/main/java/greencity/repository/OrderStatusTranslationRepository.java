package greencity.repository;

import greencity.entity.order.OrderStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Provides an interface to manage {@link OrderStatusTranslation} entity.
 *
 * @author Oleksandr Khomiakov
 */
public interface OrderStatusTranslationRepository extends JpaRepository<OrderStatusTranslation, Long> {
    /**
     * Method, that returns {@link OrderStatusTranslation} for chosen language and
     * order status.
     *
     * @return {@link OrderStatusTranslation}.
     * @author Oleksandr Khomiakov.
     */
    Optional<OrderStatusTranslation> getOrderStatusTranslationById(Long id);

    /**
     * This method which is list statuses.
     *
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    List<OrderStatusTranslation> findAllBy();
}
