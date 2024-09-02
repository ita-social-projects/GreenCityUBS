package greencity.repository;

import greencity.entity.user.locations.City;
import java.util.List;
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
}
