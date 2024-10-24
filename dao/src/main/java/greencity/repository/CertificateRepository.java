package greencity.repository;

import greencity.entity.order.Certificate;
import greencity.enums.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Set;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {
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
    Page<Certificate> findAll(Pageable page);

    /**
     * The query for searching all certificates by order id.
     *
     * @return list of {@link Certificate}.
     * @author Orest Mahdziak
     */

    @Query(
        value = "SELECT c.code, "
            + "c.status, "
            + "c.expiration_date, "
            + "c.points, "
            + "c.order_id, "
            + "c.creation_date, "
            + "c.date_of_use, "
            + "c.initial_points_value FROM ORDERS AS O JOIN CERTIFICATE AS C "
            + "ON O.ID = C.ORDER_ID WHERE O.ID = :idOrder",
        nativeQuery = true)
    List<Certificate> findCertificate(@Param("idOrder") Long idOrder);

    /**
     * The query for get all Certificate.
     *
     * @param codes is list Certificate
     * @return set of {@link Certificate}
     */
    @Query("SELECT c FROM Certificate c WHERE c.code IN :codes AND c.certificateStatus = :status")
    Set<Certificate> findByCodeInAndCertificateStatus(@Param("codes") List<String> codes,
        @Param("status") CertificateStatus status);

    /**
     * Method to check if certificate is already exist by code.
     *
     * @param code - certificate code.
     * @return return true if certificate exists and false if not.
     * @author Lilia Mokhnatska
     */
    boolean existsCertificateByCode(String code);
}