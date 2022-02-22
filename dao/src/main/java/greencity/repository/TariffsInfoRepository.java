package greencity.repository;

import greencity.entity.order.TariffsInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffsInfoRepository extends JpaRepository<TariffsInfo, Long> {
}
