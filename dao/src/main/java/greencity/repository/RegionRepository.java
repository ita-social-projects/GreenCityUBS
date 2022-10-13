package greencity.repository;

import greencity.entity.user.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

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
}
