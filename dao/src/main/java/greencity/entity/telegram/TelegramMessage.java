package greencity.entity.telegram;

import greencity.enums.MessageDeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TelegramMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime sendAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageAsset> assets;

    @Column(length = 50, unique = true)
    private String mediaGroupId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChat chat;

    @Enumerated(EnumType.STRING)
    private MessageDeliveryStatus status;

    @Column(name = "from_manager", nullable = false)
    private Boolean fromManager;

    @Column(name = "text", length = 1000)
    private String text;
}
