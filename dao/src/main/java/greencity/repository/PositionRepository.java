package greencity.repository;

import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    /**
     * Method checks if position's name already exists.
     *
     * @param name {@link String} position's name.
     * @return {@link Boolean}
     */
    boolean existsPositionByName(String name);
}
