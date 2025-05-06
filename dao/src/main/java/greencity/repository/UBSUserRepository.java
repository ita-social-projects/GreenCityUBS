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

    /**
     * Find UbsUser by order id.
     *
     * @param orderId {@link User} - id of an order.
     * @return {@link UBSuser}
     */
    @Query(value = "SELECT o.ubsUser FROM Order o WHERE o.id = :orderId")
    Optional<UBSuser> findUbsUserByOrderId(Long orderId);
}
