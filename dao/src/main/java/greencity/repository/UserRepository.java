package greencity.repository;

import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    /**
     * Method returns user by user uuid.
     *
     * @param uuid {@link String} - id to connect 2 db.
     * @return {@link User} - current user.
     */
    User findByUuid(String uuid);

    /**
     * Method returns user by user uuid.
     *
     * @param uuid {@link String} - id to connect 2 db.
     * @return optional of {@link User} - current user.
     */
    Optional<User> findUserByUuid(String uuid);

    /**
     * Method returns total user violations.
     *
     * @param userId {@link Long} - id to connect 2 db.
     * @return number of {@link User} violations.
     */

    @Query(nativeQuery = true,
        value = "select count(order_id) from violations_description_mapping as v join orders \n"
            + "on v.order_id = orders.id where users_id = :userId and v.violation_status = 'ACTIVE'")
    int countTotalUsersViolations(Long userId);

    /**
     * Method returns 1 if user has violations for the current order made by user or
     * 0 if there are no violations.
     *
     * @param userId  {@link Long} - id to connect 2 db.
     * @param orderId {@link Long}
     * @return number of {@link User} violations.
     */

    @Query(nativeQuery = true, value = "SELECT CAST(CASE WHEN EXISTS "
        + " (SELECT TRUE FROM violations_description_mapping as v join orders as o "
        + " on v.order_id = o.id "
        + " WHERE v.order_id = :orderId and o.users_id = :userId and v.violation_status = 'ACTIVE') "
        + " THEN 1 ELSE 0 END AS INT);")
    int checkIfUserHasViolationForCurrentOrder(Long userId, Long orderId);

    /**
     * Method that count orders.
     *
     * @author Struk Nazariy
     */
    @Query(nativeQuery = true, value = "SELECT users.* "
        + "FROM orders as o "
        + "JOIN users ON o.users_id = users.id "
        + "WHERE o.id = :orderId")
    Optional<User> findUserByOrderId(Long orderId);

    /**
     * Method return all user's id depends on they tariffsInfo.
     *
     * @param tariffsInfoId {@link Integer}
     * @return {@link List}of{@link User}
     * @author Hlazova Natalia
     */
    @Query(
        value = "SELECT USERS.ID FROM USERS "
            + "JOIN ORDERS "
            + "ON USERS.ID = ORDERS.USERS_ID "
            + "JOIN TARIFFS_INFO "
            + "ON TARIFFS_INFO.ID = ORDERS.TARIFFS_INFO_ID "
            + "WHERE TARIFFS_INFO.ID = :tariffsInfoId "
            + "GROUP BY USERS.ID ",
        nativeQuery = true)
    List<Long> getAllUsersByTariffsInfoId(Long tariffsInfoId);

    /**
     * Return list of the inactive users depends on time limits.
     */
    @Query(nativeQuery = true,
        value = "SELECT u.* FROM users u "
            + "INNER JOIN orders o "
            + "ON u.id = o.users_id "
            + "AND o.id = (SELECT o1.id FROM orders o1 "
            + "WHERE o1.users_id = o.users_id "
            + "ORDER BY o1.order_date DESC LIMIT 1) "
            + "WHERE CAST(o.order_date AS DATE) = :dateOfLastOrder")
    List<User> getInactiveUsersByDateOfLastOrder(LocalDate dateOfLastOrder);

    /**
     * Method sets user current points by user's id.
     *
     * @param userId       - user's ID
     * @param returnPoints - user points to set
     */
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE users SET current_points = current_points + :returnPoints WHERE id = :userId")
    void updateUserCurrentPoints(Long userId, int returnPoints);
}
