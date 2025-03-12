package greencity.repository;

import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.telegram.UnknownTelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UnknownTelegramUserRepository extends JpaRepository<UnknownTelegramUser, String> {
    /**
     * The method finds telegram bot by chatId.
     *
     * @param chatId {@link Long}.
     * @return {@link AuthorizedUser}.
     */
    Optional<UnknownTelegramUser> findByChatId(String chatId);
}
