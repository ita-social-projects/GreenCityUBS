package greencity.repository;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UBSuserRepository extends CrudRepository<UBSuser, Long> {
    /**
     * Finds a UBSuser by email.
     *
     * @param email - UBSuser's email.
     * @return a {@link Optional} of {@link UBSuser}.
     */
    Optional<UBSuser> findByEmail(String email);

    /**
     * Find UbsUser by current User.
     *
     * @param user {@link User}
     * @return {@link UBSuser}
     */
    List<UBSuser> findUBSuserByUser(User user);
}
