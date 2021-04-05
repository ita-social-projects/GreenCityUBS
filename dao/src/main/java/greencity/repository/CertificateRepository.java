package greencity.repository;

import greencity.entity.order.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
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
     * The query for searching all certificates with sorting in desc order.
     *
     * @return list of {@link Certificate}.
     * @author Nazar Struk
     */
    @Query(value = "SELECT c from Certificate c order by c.creationDate DESC")
    Page<Certificate> getAll(Pageable page);
}
