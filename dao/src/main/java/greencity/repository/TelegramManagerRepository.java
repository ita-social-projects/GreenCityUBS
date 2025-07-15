package greencity.repository;

import greencity.entity.telegram.TelegramManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramManagerRepository extends JpaRepository<TelegramManager, String> {
    /**
 * Retrieves a TelegramManager entity by its chat ID.
 *
 * @param chatId the chat ID to search for
 * @return an Optional containing the TelegramManager if found, or empty if not found
 */
Optional<TelegramManager> findByChatId(String chatId);

    /**
 * Checks if a TelegramManager entity exists with the specified chat ID.
 *
 * @param chatId the chat ID to search for
 * @return true if an entity with the given chat ID exists, false otherwise
 */
boolean existsByChatId(String chatId);
}
