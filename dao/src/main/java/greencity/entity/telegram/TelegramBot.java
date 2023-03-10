package greencity.entity.telegram;

import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;

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
