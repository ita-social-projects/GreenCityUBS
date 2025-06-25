package greencity.repository;

import greencity.entity.telegram.Image;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findByChatId(String chatId, Pageable pageable);
}
