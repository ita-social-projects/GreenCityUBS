package greencity.repository;

import greencity.entity.telegram.BotMessage;
import greencity.enums.MessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramBotMessageRepository extends JpaRepository<BotMessage, Long> {
    Optional<BotMessage> findByLangAndMessageType(String lang, MessageType messageType);
}
