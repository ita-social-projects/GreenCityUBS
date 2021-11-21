package greencity.repository;

import greencity.entity.order.OrderPaymentStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * This method which is list payment statuses found by languageId.
     *
     * @param languageId {@link Long}.
     * @return {@link List}.
     * @author Yuriy Bahlay.
     */
    @Query("SELECT ort FROM OrderPaymentStatusTranslation AS ort WHERE ort.languageId = :languageId")
    List<OrderPaymentStatusTranslation> getOrderStatusPaymentTranslationsByLanguageId(
        @Param("languageId") Long languageId);

    /**
     * This is method which is find order payment status by paymentId and
     * languageId.
     *
     * @param paymentStatusId {@link Long}.
     * @param languageId      {@link Long}.
     * @return {@link String}.
     *
     * @author Yuriy Bahlay.
     */
    @Query("SELECT ort.translationValue FROM OrderPaymentStatusTranslation AS ort "
        + "WHERE ort.orderPaymentStatusId = :paymentStatusId AND ort.languageId = :languageId")
    String findByOrderPaymentStatusIdAndLanguageIdAAndTranslationValue(@Param("paymentStatusId") Long paymentStatusId,
        @Param("languageId") Long languageId);
}
