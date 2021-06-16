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
     * Method returns violations by user id.
     *
     * @param userId {@link Integer} - id to connect 2 db.
     * @return number of {@link User} violations.
     */
    @Query(nativeQuery = true, value = "SELECT violations FROM users as v where id = :userId")
    int countUsersViolations(Long userId);

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
}
