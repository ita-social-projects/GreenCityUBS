package greencity.repository;

import greencity.entity.telegram.MessageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageAssetRepository extends JpaRepository<MessageAsset, Long> {
    List<MessageAsset> findByTelegramMessageId(Integer telegramMessageId);
}
