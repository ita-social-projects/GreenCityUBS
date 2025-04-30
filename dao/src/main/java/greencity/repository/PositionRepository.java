package greencity.repository;

import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    /**
     * Method checks if position's name already exists.
     *
     * @param name {@link String} position's name.
     * @return {@link Boolean}
     */
    boolean existsPositionByName(String name);

    /**
     * Method checks if position already exists.
     *
     * @param name {@link String} position's name.
     * @param id   {@link Long} position's id.
     * @return {@link Boolean}
     */
    boolean existsPositionByIdAndName(Long id, String name);

    /**
     * Finds all position IDs that match the given list of English names.
     *
     * @param names the list of English names to search for
     * @return a list of position IDs that have names matching the given names
     */
    @Query("SELECT p.id FROM Position p WHERE p.nameEn IN :names")
    List<Long> findAllIdsFromNames(@Param("names") List<String> names);
}
