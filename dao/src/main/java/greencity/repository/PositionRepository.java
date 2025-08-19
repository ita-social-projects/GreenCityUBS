package greencity.repository;

import greencity.entity.user.employee.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    /**
     * Method checks if position's name already exists.
     *
     * @param name {@link String} position's name.
     * @return {@link Boolean}
     */
    boolean existsPositionByNameUk(String name);

    /**
     * Finds all position IDs that match the given list of English names.
     *
     * @param names the list of English names to search for
     * @return a list of position IDs that have names matching the given names
     */
    @Query("SELECT p.id FROM Position p WHERE p.nameEn IN :names")
    List<Long> findAllIdsFromNames(@Param("names") List<String> names);

    /**
     * Finds all position that match the given list of position ids.
     *
     * @param ids the list of position ids to search for
     * @return a set of positions that matches the given ids
     */
    Set<Position> findByIdIn(Collection<Long> ids);

    long countByIdIn(Collection<Long> ids);
}
