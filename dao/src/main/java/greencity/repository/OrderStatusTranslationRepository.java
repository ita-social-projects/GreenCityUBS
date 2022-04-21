package greencity.repository;

import greencity.entity.order.OrderStatusTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * @param statusId code of the orderStatus.
     * @return {@link OrderStatusTranslation}.
     * @author Oleksandr Khomiakov.
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM order_status_translations OST"
            + " WHERE OST.status_id =:statusId")
    Optional<OrderStatusTranslation> getOrderStatusTranslationById(int statusId);

    /**
     * This method which is list statuses found by languageId.
     *
     * @param statusId {@link Long}.
     * @return {@link List}.
     *
     * @author Yuriy Bahlay.
     */
    @Query("SELECT ort FROM OrderStatusTranslation AS ort WHERE ort.statusId = :statusId")
    List<OrderStatusTranslation> getOrderStatusTranslationsId(@Param("statusId") Long statusId);
}
