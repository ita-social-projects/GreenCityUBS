package greencity.entity.viber;

import greencity.entity.user.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "viber_bot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "user")
public class ViberBot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id")
    private String chatId;
    @Column(name = "notify")
    private Boolean isNotify;
    @OneToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
