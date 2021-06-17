package greencity.repository;

import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    /**
     * Method checks if position's name already exists.
     *
     * @param position {@link String} position's name.
     * @return {@link Boolean}
     */
    boolean existsPositionByPosition(String position);

    /**
     * Method checks if position's name already exists and skips position with id.
     *
     * @param position {@link String} position's name.
     * @param id {@link Long} position's id.
     * @return {@link Boolean}
     */
    boolean existsPositionByPositionAndIdIsNot(String position, Long id);
}
