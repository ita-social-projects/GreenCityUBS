package greencity.repository;

import greencity.entity.order.OrderPaymentStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

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
     * Method, that returns {@link OrderPaymentStatusTranslation} for chosen
     * language and order status.
     *
     * @return {@link OrderPaymentStatusTranslation}.
     * @author Olet Postolovskyi.
     */
    Optional<OrderPaymentStatusTranslation> getOrderPaymentStatusTranslationById(Long id);

    /**
     * This is method which is find order payment status.
     *
     * @return {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    OrderPaymentStatusTranslation getById(Long id);
}
