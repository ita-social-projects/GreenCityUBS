package greencity.repository;

import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UBSuserRepository extends CrudRepository<UBSuser, Long> {
    /**
     * Finds list of saved user data by the id of user.
     *
     * @param userId the id of current user.
     * @return a list of {@link UBSuser} assigned to
     *         {@link greencity.entity.user.User}.
     */
    @Query("SELECT u FROM UBSuser u JOIN FETCH u.userAddress address WHERE u.user.id = :userId")
    List<UBSuser> getAllByUserId(Long userId);
}
