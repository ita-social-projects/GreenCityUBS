package greencity.entity.telegram;

import greencity.entity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = "user", callSuper = false)
@ToString(exclude = "user")
public class AuthorizedUser extends TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "notify")
    private Boolean isNotify;

    @OneToOne
    @JoinColumn(nullable = false, name = "user_id", referencedColumnName = "id")
    private User user;

    private Boolean isManager;

    public AuthorizedUser(String chatId, Boolean isSupportStatusActive, Boolean isNotify, User user,
        Boolean isManager) {
        super(chatId, isSupportStatusActive);
        this.isNotify = isNotify;
        this.user = user;
        this.isManager = isManager;
    }
}
