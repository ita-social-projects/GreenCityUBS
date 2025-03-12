package greencity.repository;

import greencity.entity.telegram.PendingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingMessageRepository extends JpaRepository<PendingMessage, String> {
    /**
     * Deletes a PendingMessage by chatId.
     *
     * @param chatId the telegram chat ID
     */
    void deleteByChatId(String chatId);

    /**
     * Finds a PendingMessage by chatId.
     *
     * @param chatId the telegram chat ID
     * @return the PendingMessage or null if not found
     */
    PendingMessage findByChatId(String chatId);
}
