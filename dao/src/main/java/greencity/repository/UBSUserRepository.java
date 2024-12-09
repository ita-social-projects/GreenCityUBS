package greencity.repository;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UBSUserRepository extends CrudRepository<UBSUser, Long> {
    /**
     * Finds a UBSUser by email.
     *
     * @param email - UBSUser's email.
     * @return a {@link Optional} of {@link UBSUser}.
     */
    Optional<UBSUser> findByEmail(String email);

    /**
     * Find UbsUser by current User.
     *
     * @param user {@link User}
     * @return {@link UBSUser}
     */
    List<UBSUser> findUBSUserByUser(User user);
}
