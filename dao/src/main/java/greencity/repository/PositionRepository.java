package greencity.repository;

import greencity.entity.enums.EmployeePosition;
import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Set<Position> findAllByPosition(List<EmployeePosition> positions);
}
