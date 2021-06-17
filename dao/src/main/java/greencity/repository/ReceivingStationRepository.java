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

    /**
     * Method checks if receiving station name already exists and skips receiving station with id.
     *
     * @param name {@link String} receiving station name.
     * @param id {@link Long} receiving station id.
     * @return {@link Boolean}
     */
    boolean existsReceivingStationByNameAndIdIsNot(String name, Long id);
}
