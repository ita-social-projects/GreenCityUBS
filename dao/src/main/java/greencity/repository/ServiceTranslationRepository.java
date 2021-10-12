package greencity.repository;

import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTranslationRepository extends JpaRepository<ServiceTranslation, Long> {
    /**
     * Method find service translation by service and language code.
     * 
     * @param service - current service.
     * @param code    - language code.
     * @return {@link ServiceTranslation}
     * @author Vadym Makitra
     */
    ServiceTranslation findServiceTranslationsByServiceAndLanguageCode(Service service, String code);
}
