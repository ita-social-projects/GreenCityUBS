package greencity.entity.telegram;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class UnknownTelegramUser extends TelegramUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(name = "username")
    private String userName;
    private String mobileNumber;

    public UnknownTelegramUser(String chatId, Boolean isSupportStatusActive,
        String firstName, String lastName,
        String userName, String mobileNumber) {
        super(chatId, isSupportStatusActive);
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.mobileNumber = mobileNumber;
    }
}
