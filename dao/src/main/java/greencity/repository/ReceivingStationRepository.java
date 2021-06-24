package greencity.repository;

import greencity.entity.user.employee.ReceivingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivingStationRepository extends JpaRepository<ReceivingStation, Long> {
    /**
     * Method checks if receiving station name already exists.
     *
     * @param name {@link String} receiving station name.
     * @return {@link Boolean}
     */
    boolean existsReceivingStationByName(String name);
}
