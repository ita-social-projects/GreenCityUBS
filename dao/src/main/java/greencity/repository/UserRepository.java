package greencity.repository;

import greencity.entity.user.User;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
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
        value = "SELECT COUNT(user_id) FROM violations_description_mapping as v where v.user_id = :userId")
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
        + "(SELECT TRUE FROM violations_description_mapping "
        + " AS v WHERE v.user_id = :userId and v.order_id = :orderId)\n"
        + " THEN 1 ELSE 0 END AS INT);")
    int checkIfUserHasViolationForCurrentOrder(Long userId, Long orderId);

    /**
     * Method that count orders.
     *
     * @author Struk Nazariy
     */
    @Query(nativeQuery = true, value = "select count(*) from orders")
    int orderCounter();

    /**
     * Method that count orders.
     *
     * @author Struk Nazariy
     */
    @Query(nativeQuery = true, value = "select count(*) from orders")
    int orderCounterForSorting();

    /**
     * Method that count orders.
     *
     * @author Struk Nazariy
     */
    @Query(nativeQuery = true, value = "SELECT users.* " +
        "FROM orders as o " +
        "JOIN users ON o.users_id = users.id " +
        "WHERE o.id = :orderId")
    Optional<User> findUserByOrderId(Long orderId);
}
