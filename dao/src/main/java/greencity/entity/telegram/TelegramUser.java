package greencity.entity.telegram;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class TelegramUser {
    @Column(nullable = false)
    private String chatId;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean isSupportStatusActive;

    protected TelegramUser(String chatId, Boolean isSupportStatusActive) {
        this.chatId = chatId;
        this.isSupportStatusActive = isSupportStatusActive;
    }
}
