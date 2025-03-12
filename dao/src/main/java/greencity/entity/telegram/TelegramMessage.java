package greencity.entity.telegram;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class TelegramMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;
    private String chatId;
    private boolean isRead;
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime sendAt;

    public void read() {
        this.isRead = true;
    }

    protected TelegramMessage(String chatId, Boolean isRead) {
        this.chatId = chatId;
        this.isRead = isRead;
    }
}
