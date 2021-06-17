package greencity.repository;

import greencity.entity.user.employee.ReceivingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivingStationRepository extends JpaRepository<ReceivingStation, Long> {
    /**
     * Method checks if receiving station name already exists.
     *
     * @param receivingStation {@link String} receiving station name.
     * @return {@link Boolean}
     */
    boolean existsReceivingStationByReceivingStation(String receivingStation);

    /**
     * Method checks if receiving station name already exists and skips receiving station with id.
     *
     * @param receivingStation {@link String} receiving station name.
     * @param id {@link Long} receiving station id.
     * @return {@link Boolean}
     */
    boolean existsReceivingStationByReceivingStationAndIdIsNot(String receivingStation, Long id);
}
