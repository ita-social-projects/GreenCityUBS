package greencity.repository;

import greencity.entity.user.locations.District;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DistrictRepository extends JpaRepository<District, Long> {
    /**
     * Retrieves all districts from the database. This method returns a list of all
     * {@link District} entities present in the database.
     *
     * @return a list of {@link District} entities.
     */
    List<District> findAll();

    /**
     * Retrieves all districts that belong to the specified city. This method
     * filters the districts based on the given city ID and returns a list of
     * {@link District} entities that are associated with that city.
     *
     * @param cityId the ID of the city for which districts are to be retrieved.
     * @return a list of {@link District} entities that belong to the specified
     *         city.
     */
    List<District> findAllByCityId(Long cityId);

    /**
     * Finds a district by its city ID, where either the English or Ukrainian name
     * matches.
     *
     * @param cityId the ID of the city to which the district belongs
     * @param nameEn the English name of the district
     * @param nameUk the Ukrainian name of the district
     * @return an {@code Optional<District>} containing the found district if it
     *         exists, or an empty {@code Optional} if no match is found
     * @author Kizerov Dmytro
     */
    @Query("select d from District d where d.city.id = :cityId and (d.nameEn = :nameEn or d.nameUk = :nameUk)")
    Optional<District> findDistrictByCityIdAndNameEnOrNameUk(Long cityId, String nameEn, String nameUk);
}
