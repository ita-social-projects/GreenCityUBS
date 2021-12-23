package greencity.repository;

import greencity.entity.user.RegionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionTranslationRepository extends JpaRepository<RegionTranslation, Long> {
}
