package greencity.repository;

import greencity.entity.user.locations.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CityRepository extends JpaRepository<City, Long> {
    /**
     * Retrieves all cities from the database. This method returns a list of all
     * {@link City} entities present in the database.
     *
     * @return a list of {@link City} entities.
     */
    List<City> findAll();

    /**
     * Retrieves all cities that belong to the specified region. This method filters
     * the cities based on the given region ID and returns a list of {@link City}
     * entities that are associated with that region.
     *
     * @param regionId the ID of the region for which cities are to be retrieved.
     * @return a list of {@link City} entities that belong to the specified region.
     */
    List<City> findAllByRegionId(Long regionId);

    /**
     * Retrieves all cities from the database, including their associated districts.
     * This method uses JPQL with a LEFT JOIN FETCH clause to ensure that cities and
     * their districts are fetched in a single query, which helps to avoid the "N+1
     * selects" problem.
     *
     * @return a list of all cities, each with their associated districts fully
     *         initialized.
     */
    @Query("SELECT c from City c left join fetch c.districts")
    List<City> findAllCitiesWithDistricts();

    /**
     * Finds a city by its region ID, Ukrainian name, and English name.
     *
     * @param regionId the ID of the region to which the city belongs
     * @param nameUk   the Ukrainian name of the city
     * @param nameEn   the English name of the city
     * @return an {@code Optional<City>} containing the found city if it exists, or
     *         an empty {@code Optional} if not found
     * @author Kizerov Dmytro
     */
    @Query(
        value = "SELECT c.* FROM cities c WHERE c.region_id = :regionId"
            + " AND (c.name_uk = :nameUk OR c.name_en = :nameEn) LIMIT 1",
        nativeQuery = true)
    Optional<City> findCityByRegionIdAndNameUkAndNameEn(Long regionId, String nameUk, String nameEn);
}
