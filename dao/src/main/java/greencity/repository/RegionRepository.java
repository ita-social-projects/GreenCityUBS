package greencity.repository;

import greencity.entity.user.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    /**
     * Method for get info about region.
     *
     * @param name - name of searching region
     * @return {@link Region}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM regions r "
            + "WHERE r.name_en = :name "
            + "OR r.name_uk = :name")
    Optional<Region> findRegionByName(@Param("name") String name);
}
