package greencity.repository;

import greencity.entity.telegram.BotResponseProjection;
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
            t.id AS id,
            t.message_type AS messageType,
            t.lang AS lang,
            t.text AS text
        FROM bot_messages t
        ORDER BY t.message_type, t.lang
        """,
        countQuery = "SELECT COUNT(*) FROM bot_messages",
        nativeQuery = true)
    Page<BotResponseProjection> findAllWithLang(Pageable pageable);
}
