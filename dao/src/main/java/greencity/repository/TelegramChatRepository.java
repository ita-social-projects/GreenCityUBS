package greencity.repository;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramChatRepository
    extends JpaRepository<TelegramChat, Long>, JpaSpecificationExecutor<TelegramChat> {
    /**
     * The method finds telegram bot by user and chat id and isNotify.
     *
     * @param user     {@link User}.
     * @param chatId   {@link Long}.
     * @param isNotify {@link Boolean}
     * @return {@link Optional} {@link TelegramChat}.
     *
     * @author Julia Seti
     */
    Optional<TelegramChat> findByUserAndChatIdAndIsNotify(User user, String chatId, Boolean isNotify);

    /**
     * The method finds telegram bot by user.
     *
     * @param user {@link User}.
     * @return {@link Optional} {@link TelegramChat}.
     *
     * @author Julia Seti
     */
    Optional<TelegramChat> findByUser(User user);

    /**
     * The method finds telegram bot by chatId.
     *
     * @param chatId {@link Long}.
     * @return {@link TelegramChat}.
     */
    Optional<TelegramChat> findByChatId(String chatId);
}
