package greencity.repository;

import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query(nativeQuery = true,
        value = "select * from location_translations lt "
            + "join languages lang on lt.language_id = lang.id "
            + "join locations l on lt.location_id = l.id "
            + "join courier c on l.id = c.location_id "
            + "where c.id = :courierId and lang.code = :languageCode")
    List<LocationTranslation> findLocationTranslationByCourierIdAndLanguageCode(@Param("courierId") Long courierId,
        @Param("languageCode") String languageCode);
}
