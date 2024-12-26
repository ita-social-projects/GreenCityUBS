package greencity.repository;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UBSUserRepository extends CrudRepository<UBSuser, Long> {
    /**
     * Find UbsUser by current User.
     *
     * @param user {@link User}
     * @return {@link UBSuser}
     */
    List<UBSuser> findUBSuserByUser(User user);
}
