package greencity.repository;

import greencity.entity.telegram.MessageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAssetRepository extends JpaRepository<MessageAsset, Long> {
}
