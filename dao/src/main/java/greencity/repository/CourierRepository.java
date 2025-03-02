package greencity.repository;

import greencity.entity.order.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    /**
     * Method to check if the courier exists by courier id.
     *
     * @param id - courier id.
     * @return return true if courier exists and false if not.
     * @author Nikita Korzh.
     */
    boolean existsCourierById(Long id);

    /**
     * Method for getting all active couriers.
     *
     * @return list of {@link Courier}
     *
     * @author Anton Bondar
     */
    @Query(value = "SELECT c FROM Courier c WHERE c.courierStatus = 'ACTIVE'")
    List<Courier> getAllActiveCouriers();
}
