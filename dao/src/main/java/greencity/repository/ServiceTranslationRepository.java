package greencity.repository;

import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTranslationRepository extends JpaRepository<ServiceTranslation, Long> {
//    ServiceTranslation findServiceTranslationsByServiceandAndLanguageCode(Service service, String code);
}
