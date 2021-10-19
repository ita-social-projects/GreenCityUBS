package greencity.repository;

import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierTranslationRepository extends JpaRepository<CourierTranslation, Long> {
    /**
     * Find courier translation by courier.
     * 
     * @param courier {@link Courier}
     * @return {@link CourierTranslation}
     */
    CourierTranslation findCourierTranslationByCourier(Courier courier);
}
