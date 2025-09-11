package greencity.entity.telegram;

import greencity.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"user"})
@Table(name = "telegram_bot")
public class TelegramBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "chat_id")
    private Long chatId;
    @Column(nullable = false, name = "notify")
    private Boolean isNotify;
    @OneToOne()
    @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id")
    private User user;
}
