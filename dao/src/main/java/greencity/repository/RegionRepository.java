package greencity.repository;

import greencity.entity.user.Region;
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
    Optional<Region> findRegionByEnNameAndUkrName(@Param("EnName") String nameEn,
        @Param("UkrName") String nameUk);

    /**
     * Method for get regions with only active locations.
     * 
     * @return List of {@link Region} if at least one region exists
     * @author Safarov Renat
     */
    @Query(nativeQuery = true,
        value = "select * from regions r "
            + "join locations l on r.id = l.region_id "
            + "where l.location_status = 'ACTIVE' "
            + "group by r.id, l.id")
    List<Region> findRegionsWithActiveLocations();
}
