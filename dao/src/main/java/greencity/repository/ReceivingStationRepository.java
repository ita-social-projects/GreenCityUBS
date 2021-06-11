package greencity.repository;

import greencity.entity.user.employee.ReceivingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReceivingStationRepository extends JpaRepository<ReceivingStation, Long> {

}
