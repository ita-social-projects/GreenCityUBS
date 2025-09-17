package greencity.repository;

import greencity.dto.BotResponseProjection;
import greencity.entity.telegram.BotMessage;
import greencity.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramBotMessageRepository extends JpaRepository<BotMessage, Long> {
    Optional<BotMessage> findByLangAndMessageType(String lang, MessageType messageType);

    @Query(value = """
        SELECT
            t.message_type AS messageType,
            MAX(CASE WHEN t.lang = 'uk' THEN t.text END) AS messageUk,
            MAX(CASE WHEN t.lang = 'en' THEN t.text END) AS messageEn
        FROM bot_messages t
        GROUP BY t.message_type
        ORDER BY t.message_type
        """,
        countQuery = "SELECT COUNT(DISTINCT message_type) FROM bot_messages",
        nativeQuery = true)
    Page<BotResponseProjection> findAllPivot(Pageable pageable);
}
