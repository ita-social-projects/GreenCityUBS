package greencity.repository;

import greencity.entity.telegram.ChatFeedback;
import greencity.enums.FeedbackState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {
    @Query(value = "SELECT * FROM chat_feedback WHERE chat_id = :chatId LIMIT 1", nativeQuery = true)
    Optional<ChatFeedback> findByChatId(@Param("chatId") String chatId);

    @Query(value = "SELECT * FROM chat_feedback WHERE chat_id = :chatId", nativeQuery = true)
    Page<ChatFeedback> findByChatIdPageable(String chatId, Pageable pageable);

    Optional<ChatFeedback> findByChatIdAndFeedbackState(Long chatId, FeedbackState feedbackState);
}
