package greencity.repository;

import greencity.entity.telegram.TelegramManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramManagerRepository extends JpaRepository<TelegramManager, String> {
    Optional<TelegramManager> findByChatId(String chatId);
}
