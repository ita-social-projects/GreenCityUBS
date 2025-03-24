package greencity.repository;

import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AuthorizedUserRepository extends JpaRepository<AuthorizedUser, Long> {
    /**
     * The method finds telegram bot by user and chat id and isNotify.
     *
     * @param user     {@link User}.
     * @param chatId   {@link Long}.
     * @param isNotify {@link Boolean}
     * @return {@link Optional} {@link AuthorizedUser}.
     *
     * @author Julia Seti
     */
    Optional<AuthorizedUser> findByUserAndChatIdAndIsNotify(User user, String chatId, Boolean isNotify);

    /**
     * The method finds telegram bot by user.
     *
     * @param user {@link User}.
     * @return {@link Optional} {@link AuthorizedUser}.
     *
     * @author Julia Seti
     */
    Optional<AuthorizedUser> findByUser(User user);

    /**
     * The method finds telegram bot by chatId.
     *
     * @param chatId {@link Long}.
     * @return {@link AuthorizedUser}.
     */
    Optional<AuthorizedUser> findByChatId(String chatId);

    /**
     * The method finds all telegram users.
     *
     * @param pageable {@link Pageable}.
     * @return {@link Page} of {@link AuthorizedUser}.
     *
     */
    @Query("select a from AuthorizedUser a ")
    Page<AuthorizedUser> findAllUsers(Pageable pageable);
}
