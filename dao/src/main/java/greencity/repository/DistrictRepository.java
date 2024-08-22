package greencity.repository;

import greencity.entity.user.locations.District;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {
	/**
	 * Retrieves all districts from the database.
	 * This method returns a list of all {@link District} entities present in the database.
	 *
	 * @return a list of {@link District} entities.
	 */
	List<District> findAll();

	/**
	 * Retrieves all districts that belong to the specified city.
	 * This method filters the districts based on the given city ID and returns a list of {@link District}
	 * entities that are associated with that city.
	 *
	 * @param cityId the ID of the city for which districts are to be retrieved.
	 * @return a list of {@link District} entities that belong to the specified city.
	 */
	List<District> findAllByCityId(Long cityId);
}
