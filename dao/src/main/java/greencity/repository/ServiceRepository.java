package greencity.repository;

import greencity.entity.order.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Method that return service by TariffsInfo id.
     *
     * @param tariffId {@link Long} - TariffsInfo id
     * @return {@link Optional} of {@link Service}
     * @author Julia Seti
     */
    Optional<Service> findServiceByTariffsInfoId(Long tariffId);
}
