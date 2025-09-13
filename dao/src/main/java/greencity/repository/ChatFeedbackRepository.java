package greencity.repository;

import greencity.entity.telegram.ChatFeedback;
import greencity.enums.FeedbackState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {
    /**
     * Finds all {@link ChatFeedback} entities for the specified chat ID with
     * pagination support.
     *
     * @param chatId   the ID of the chat whose feedback records should be retrieved
     * @param pageable pagination information (page number, size, sorting)
     * @return a page containing {@link ChatFeedback} entities for the given chat ID
     */
    Page<ChatFeedback> findByChatId(Long chatId, Pageable pageable);

    /**
     * Finds a {@link ChatFeedback} entity for the specified chat ID and feedback
     * state.
     *
     * @param chatId        the ID of the chat whose feedback should be retrieved
     * @param feedbackState the state of the feedback to search for
     * @return an {@link Optional} containing the matching {@link ChatFeedback} if
     *         found, otherwise empty
     */
    Optional<ChatFeedback> findByChatIdAndFeedbackState(Long chatId, FeedbackState feedbackState);
}
