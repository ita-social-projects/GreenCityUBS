package greencity.repository;

import greencity.entity.telegram.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, Long> {
}
