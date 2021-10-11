package greencity.repository;

import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationTranslationRepository extends JpaRepository<LocationTranslation, Long> {
    /**
     * Find Location Translation by Location.
     * 
     * @param location {@link Location}
     * @return {@link LocationTranslation}
     * @author Vadym Makitra
     */
    Optional<LocationTranslation> findLocationTranslationByLocationAndLanguageCode(Location location, String code);
}
