package greencity.repository;

import greencity.entity.telegram.BotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramBotMessageRepository extends JpaRepository<BotMessage, Long> {
}
