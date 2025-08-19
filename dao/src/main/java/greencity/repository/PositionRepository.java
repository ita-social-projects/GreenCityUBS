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
     * Checks if a position with the given Ukrainian name already exists.
     *
     * @param name the Ukrainian name of the position to check
     * @return {@code true} if a position with the given name exists, {@code false}
     *         otherwise
     */
    boolean existsPositionByNameUk(String name);

    /**
     * Finds all position IDs corresponding to the given list of English names.
     *
     * @param names a list of English names of positions to find IDs for
     * @return a list of position IDs that have names matching the given names
     */
    @Query("SELECT p.id FROM Position p WHERE p.nameEn IN :names")
    List<Long> findAllIdsFromNames(@Param("names") List<String> names);

    /**
     * Finds all positions that match any of the given IDs.
     *
     * @param ids a collection of position IDs to search for
     * @return a set of {@link Position} entities whose IDs are in the given
     *         collection
     */
    Set<Position> findByIdIn(Collection<Long> ids);
}
