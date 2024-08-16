package greencity.repository;

import greencity.entity.telegram.TelegramBot;
import greencity.entity.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramBotRepository extends CrudRepository<TelegramBot, Long> {
    /**
     * The method finds telegram bot by user and chat id and isNotify.
     *
     * @param user     {@link User}.
     * @param chatId   {@link Long}.
     * @param isNotify {@link Boolean}
     * @return {@link Optional} {@link TelegramBot}.
     *
     * @author Julia Seti
     */
    Optional<TelegramBot> findByUserAndChatIdAndIsNotify(User user, Long chatId, Boolean isNotify);

    /**
     * The method finds telegram bot by user.
     *
     * @param user {@link User}.
     * @return {@link Optional} {@link TelegramBot}.
     *
     * @author Julia Seti
     */
    Optional<TelegramBot> findByUserAndIsNotifyIsTrue(User user);
}
