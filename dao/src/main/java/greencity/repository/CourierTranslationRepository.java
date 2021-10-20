package greencity.repository;

import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourierTranslationRepository extends JpaRepository<CourierTranslation, Long> {
    /**
     * Find courier translation by courier and language id.
     *
     * @param courier    {@link Courier}
     * @param languageId - id of current language.
     * @return {@link CourierTranslation}
     */
    CourierTranslation findCourierTranslationByCourierAndLanguageId(Courier courier, Long languageId);
}
