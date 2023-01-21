package greencity.repository;

import greencity.entity.order.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Method that return service by id.
     *
     * @param id {@link Long}
     * @return {@link Service}
     * @author Vadym Makitra
     */
    Optional<Service> findServiceById(Long id);

    /**
     * Method that return service by TariffsInfo id.
     *
     * @param tariffId {@link Long} - TariffsInfo id
     * @return {@link Optional} of {@link Service}
     * @author Julia Seti
     */
    Optional<Service> findServiceByTariffsInfoId(Long tariffId);
}
