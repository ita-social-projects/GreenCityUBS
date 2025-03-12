package greencity.entity.telegram;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
public class TextMessage extends TelegramMessage {
    private String text;

    private boolean isManagerMessage;

    public TextMessage(String chatId, Boolean isRead, String text, boolean isManagerMessage) {
        super(chatId, isRead);
        this.text = text;
        this.isManagerMessage = isManagerMessage;
    }
}
