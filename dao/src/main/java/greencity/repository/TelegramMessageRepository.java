package greencity.repository;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    /**
     * Retrieves all TelegramUserMessages by chatId.
     *
     * @param chatId the telegram chat ID
     * @return a list of TelegramUserMessages associated with the specified chatId
     */
    Page<TelegramMessage> findByChatId(Long chatId, Pageable pageable);

    Optional<TelegramMessage> findByMediaGroupId(String mediaGroupId);

    Optional<TelegramMessage> findFirstByChatOrderBySendAtDesc(TelegramChat chat);
}
