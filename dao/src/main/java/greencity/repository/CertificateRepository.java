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

    @Query(value = "SELECT * FROM ORDERS AS O JOIN CERTIFICATE AS C "
        + "ON O.ID = C.ORDER_ID WHERE O.ID = :idOrder", nativeQuery = true)
    List<Certificate> findCertificate(@Param("idOrder") Long idOrder);

    /**
     * The query for get all Certificate.
     *
     * @param code is list Certificate
     * @return set of {@link Certificate}
     */
    Set<Certificate> findAllByCodeAndCertificateStatus(List<String> code, CertificateStatus status);

    /**
     * Method to check if certificate is already exist by code.
     *
     * @param code - certificate code.
     * @return return true if certificate exists and false if not.
     * @author Lilia Mokhnatska
     */
    boolean existsCertificateByCode(String code);
}