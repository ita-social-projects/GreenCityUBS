package greencity.repository;

import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
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
    @Query("SELECT u FROM UBSuser u JOIN FETCH u.address address WHERE u.user.id = :userId")
    List<UBSuser> getAllByUserId(Long userId);

    /**
     * Finds list of UBSuser who have not paid of the order within three days.
     *
     * @param localDate - date when the user made an order.
     * @return a {@link List} of {@link UBSuser} - which need to send a message.
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM ubs_user u INNER JOIN orders o ON u.id = o.ubs_user_id "
            + "WHERE CAST(o.order_date AS DATE) = :localDate AND o.order_status LIKE 'FORMED'")
    List<UBSuser> getAllUBSusersWhoHaveNotPaid(LocalDate localDate);


    /**
     * Finds list of UBSuser who have no orders after {@param localDate}.
     *
     * @param localDate - date after which user have no orders.
     * @return a {@link List} of {@link UBSuser} - which have no orders after {@param localDate}.
     */
    @Query(nativeQuery = true,
            value = "SELECT * FROM ubs_user as u INNER JOIN orders as o ON u.id = o.ubs_user_id " +
                    "WHERE (SELECT COUNT(id) FROM orders WHERE CAST(o.order_date AS DATE) < :localDate)!=0")
    List<UBSuser> getAllInactiveUsers(LocalDate localDate);
}
