package greencity.repository;

import greencity.entity.telegram.UnknownTelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnknownTelegramUserRepository extends JpaRepository<UnknownTelegramUser, Long> {
}
