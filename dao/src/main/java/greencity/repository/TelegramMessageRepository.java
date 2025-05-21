package greencity.repository;

import greencity.entity.telegram.TextMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramMessageRepository extends JpaRepository<TextMessage, Long> {
    /**
     * Retrieves all TelegramUserMessages by chatId.
     *
     * @param chatId the telegram chat ID
     * @return a list of TelegramUserMessages associated with the specified chatId
     */
    Page<TextMessage> findByChatId(String chatId, Pageable pageable);

    boolean existsByChatId(String chatId);
}
