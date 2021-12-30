package greencity.entity.telegram;

import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "telegram_bot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
public class TelegramBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id")
    private Long chatId;
    @OneToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
