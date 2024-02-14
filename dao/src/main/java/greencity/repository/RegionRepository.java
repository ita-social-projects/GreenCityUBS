package greencity.repository;

import greencity.entity.user.Region;
import greencity.enums.LocationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    /**
     * Method for get info about region.
     *
     * @param nameEn - name of searching region in English
     * @param nameUk - name of searching region in Ukrainian
     * @return Optional of {@link Region} if one of the params matches
     * @author Vadym Makitra
     * @author Yurii Fedorko
     */
    Optional<Region> findRegionByEnNameAndUkrName(@Param("EnName") String nameEn,
        @Param("UkrName") String nameUk);

    /**
     * Method that retrieves regions with locations specified by LocationStatus
     * type.
     *
     * @param locationStatus {@link LocationStatus} - status of searched locations.
     * @return List of {@link Region} if at least one region exists.
     * @author Maksym Lenets
     */
    @EntityGraph(attributePaths = "locations")
    Optional<List<Region>> findAllByLocationsLocationStatus(LocationStatus locationStatus);

    /**
     * Method to check if the region exists by regionId.
     *
     * @param id - region id.
     * @return return true if region exists and false if not.
     * @author Nikita Korzh.
     */
    boolean existsRegionById(Long id);
}
