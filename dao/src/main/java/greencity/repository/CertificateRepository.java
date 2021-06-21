package greencity.repository;

import greencity.entity.order.Certificate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
/**
 * The method returns all certificates with sorting in desc order.
 *
 * @return list of {@link Certificate}.
 * @author Nazar Struk
 */
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

    /**
     * The query for searching all certificates with sorting in desc order.
     *
     * @return list of {@link Certificate}.
     * @author Nazar Struk
     */
    @Query(value = "SELECT c from Certificate c order by c.creationDate DESC")
    Page<Certificate> getAll(Pageable page);

    /**
     * The query for searching all certificates by order id.
     *
     * @return list of {@link Certificate}.
     * @author Orest Mahdziak
     */

    @Query(value = "SELECT * FROM ORDERS AS O JOIN CERTIFICATE AS C "
        + "ON O.ID = C.ORDER_ID WHERE O.ID = :idOrder", nativeQuery = true)
    List<Certificate> findCertificate(@Param("idOrder") Long idOrder);
}
