package greencity.repository;

import greencity.entity.order.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    /**
     * Method that returns active service by id.
     *
     * @param serviceId {@link Long} - service id
     * @return {@link Optional} of {@link Service}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM service "
            + "WHERE id = :serviceId AND status = 'ACTIVE'")
    Optional<Service> findActiveServiceById(Long serviceId);

    /**
     * Method that returns active service by TariffsInfo id.
     *
     * @param tariffId {@link Long} - TariffsInfo id
     * @return {@link Optional} of {@link Service}
     * @author Julia Seti
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM service "
            + "WHERE tariffs_info_id = :tariffInfoId AND status = 'ACTIVE'")
    Optional<Service> findActiveServiceByTariffsInfoId(Long tariffId);
}
