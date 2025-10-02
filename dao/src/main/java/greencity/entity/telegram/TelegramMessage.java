package greencity.entity.telegram;

import greencity.enums.MessageDeliveryStatus;
import greencity.enums.MessageViewingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"chat", "assets"})
@EqualsAndHashCode(exclude = {"chat", "assets"})
@EntityListeners(AuditingEntityListener.class)
public class TelegramMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "telegram_message_id")
    private Integer telegramMessageId = 0;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant sendAt;

    @LastModifiedDate
    private Instant updatedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAsset> assets = new ArrayList<>();

    @Column(length = 50, unique = true)
    private String mediaGroupId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private TelegramChat chat;

    @Enumerated(EnumType.STRING)
    private MessageDeliveryStatus status;

    @Enumerated(EnumType.STRING)
    private MessageViewingStatus messageViewingStatus;

    @Column(name = "from_manager", nullable = false)
    private Boolean fromManager;

    @Column(name = "text", length = 1000)
    private String text;
}
