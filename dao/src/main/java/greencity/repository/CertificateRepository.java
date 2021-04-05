package greencity.repository;

import greencity.entity.order.Certificate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends CrudRepository<Certificate, String> {
    /**
     * Method update status to expired for all {@link Certificate} in which
     * expiration date is off.
     */
    @Modifying
    @Query(value = "update Certificate set certificateStatus = 'EXPIRED' "
        + "where expirationDate < current_date and "
        + "certificateStatus in ('ACTIVE', 'NEW')")
    void updateCertificateStatusToExpired();
}
