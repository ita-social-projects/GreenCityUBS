package greencity.repository;

import greencity.entity.user.Region;
import greencity.enums.LocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    Optional<Region> findRegionByNameEnOrNameUk(@Param("nameEn") String nameEn,
        @Param("nameUk") String nameUk);

    /**
     * Method that retrieves regions with locations specified by LocationStatus
     * type.
     *
     * @param locationStatus {@link LocationStatus} - status of searched locations.
     * @return List of {@link Region} if at least one region exists.
     * @author Maksym Lenets
     */
    @Query("SELECT r FROM Region r "
        + "LEFT JOIN FETCH r.locations l "
        + "WHERE l.isDeleted = false "
        + "AND l.locationStatus = :locationStatus")
    Optional<List<Region>> findAllWithLocationsByLocationStatus(@Param("locationStatus") LocationStatus locationStatus);

    /**
     * Method to check if the region exists by regionId.
     *
     * @param id - region id.
     * @return return true if region exists and false if not.
     * @author Nikita Korzh.
     */
    boolean existsRegionById(Long id);

    /**
     * Method to find all regions with not deleted locations.
     *
     * @return return list of regions.
     * @author Denys Ryhal.
     */
    @Query("SELECT r FROM Region r "
        + "LEFT JOIN FETCH r.locations l "
        + "WHERE l.isDeleted = false")
    List<Region> findAllWithNotDeletedLocations();

    /**
     * Retrieves all regions from the database.
     *
     * @return a list of {@link Region} entities, each representing a region from
     *         the database.
     */
    List<Region> findAll();

    /**
     * Retrieves all regions from the database, including their associated cities
     * and districts. This method uses JPQL with multiple LEFT JOIN FETCH clauses to
     * ensure that regions, cities, and districts are all fetched in a single query,
     * avoiding the "N+1 selects" problem.
     *
     * @return a list of all regions, each with their associated cities and
     *         districts fully initialized.
     */
    @Query("SELECT DISTINCT r FROM Region r "
        + "LEFT JOIN FETCH r.cities c "
        + "LEFT JOIN FETCH c.districts d")
    List<Region> findAllRegionsWithCitiesAndDistricts();

    /**
     * Retrieves all regions from the database, including their associated cities.
     * This method uses JPQL with a LEFT JOIN FETCH clause to ensure that regions
     * and their cities are fetched in a single query, which helps to avoid the "N+1
     * selects" problem.
     *
     * @return a list of all regions, each with their associated cities fully
     *         initialized.
     */
    @Query("SELECT r from Region r left join fetch r.cities")
    List<Region> findAllRegionsWithCities();
}
