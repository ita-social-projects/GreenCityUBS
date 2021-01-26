package greencity.dao.repository;

import greencity.dao.entity.order.Certificate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends CrudRepository<Certificate, String> {
}
