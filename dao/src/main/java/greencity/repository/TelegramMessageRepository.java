package greencity.repository;

import greencity.entity.telegram.TelegramMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    /**
     * Retrieves all {@link TelegramMessage} entities for a given chat ID, with
     * pagination support.
     *
     * @param chatId   {@link Long} the Telegram chat ID
     * @param pageable {@link Pageable} the pagination information
     * @return a {@link Page} of {@link TelegramMessage} objects associated with the
     *         specified chat
     */
    Page<TelegramMessage> findByChatId(Long chatId, Pageable pageable);

    /**
     * Retrieves a {@link TelegramMessage} by its media group ID. Telegram assigns
     * the same mediaGroupId to multiple media messages sent as an album.
     *
     * @param mediaGroupId {@link String} the media group ID
     * @return an {@link Optional} containing the {@link TelegramMessage} if found,
     *         otherwise empty
     */
    Optional<TelegramMessage> findByMediaGroupId(String mediaGroupId);
}
