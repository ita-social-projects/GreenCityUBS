package greencity.entity.telegram;

import greencity.enums.FeedbackState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;

    private String comment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackState feedbackState = FeedbackState.IN_PROGRESS;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChat chat;
}
