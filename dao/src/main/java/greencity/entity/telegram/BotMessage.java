package greencity.entity.telegram;

import greencity.enums.MessageType;
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
@Table(name = "bot_messages")
public class BotMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, unique = true)
    private MessageType messageType;

    @Column(name = "message_uk", columnDefinition = "TEXT")
    private String messageUk;

    @Column(name = "message_en", columnDefinition = "TEXT")
    private String messageEn;
}
