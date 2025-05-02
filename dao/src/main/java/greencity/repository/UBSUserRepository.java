package greencity.repository;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UBSUserRepository extends CrudRepository<UBSuser, Long> {
    /**
     * Find UbsUser by current User.
     *
     * @param user {@link User}
     * @return {@link UBSuser}
     */
    List<UBSuser> findUBSuserByUser(User user);

    @Query(value = "SELECT * FROM ubs_user JOIN orders on ubs_user.id = orders.ubs_user_id WHERE orders.id = :orderId",
        nativeQuery = true)
    Optional<UBSuser> findUbsUserByOrderId(Long orderId);
}
