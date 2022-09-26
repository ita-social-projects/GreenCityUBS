package greencity.repository;

import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTranslationRepository extends JpaRepository<ServiceTranslation, Long> {
    /**
     * Method find service translation by service.
     * 
     * @param service - current service.
     * @return {@link ServiceTranslation}
     * @author Vadym Makitra
     */
    ServiceTranslation findServiceTranslationsByService(Service service);
}
