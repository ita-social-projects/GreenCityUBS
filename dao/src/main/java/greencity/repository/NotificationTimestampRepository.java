package greencity.repository;

import greencity.entity.telegram.NotificationTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.Optional;

@Repository
public interface NotificationTimestampRepository extends JpaRepository<NotificationTimestamp, String> {
    /**
     * Finds a NotificationTimestamp entity by chat ID.
     *
     * @param chatId the chat ID to search for
     * @return an Optional containing the NotificationTimestamp if found, or an
     *         empty Optional if not found
     */
    Optional<NotificationTimestamp> findByChatId(String chatId);

    /**
     * Finds the last notification time by chat ID.
     *
     * @param chatId the chat ID to search for
     * @return the last notification time as an Instant, or null if not found
     */
    @Query("SELECT n.lastNotificationTime FROM NotificationTimestamp n WHERE n.chatId = :chatId")
    Instant findInstantByChatId(@Param("chatId") String chatId);
}
