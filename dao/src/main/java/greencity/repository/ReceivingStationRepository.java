package greencity.repository;

import greencity.entity.user.employee.ReceivingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceivingStationRepository extends JpaRepository<ReceivingStation, Long> {
}
