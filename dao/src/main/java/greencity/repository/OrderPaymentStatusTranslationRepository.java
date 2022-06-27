package greencity.repository;

import greencity.entity.order.OrderPaymentStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Provides an interface to manage
 * {@link OrderPaymentStatusTranslationRepository} entity.
 *
 * @author Yuriy Bahlay.
 */
public interface OrderPaymentStatusTranslationRepository
    extends JpaRepository<OrderPaymentStatusTranslation, Long> {
    /**
     * This method which is list payment statuses.
     *
     * @return {@link List}.
     * @author Yuriy Bahlay.
     */
    List<OrderPaymentStatusTranslation> getAllBy();

    /**
     * This is method which is find order payment status.
     *
     * @return {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    OrderPaymentStatusTranslation getById(Long id);
}
