package greencity.entity.telegram;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
public class Image extends TelegramMessage {
    private String fileUrl;

    private String caption;

    public Image(String chatId, Boolean isRead, String fileUrl, String caption) {
        super(chatId, isRead);
        this.fileUrl = fileUrl;
        this.caption = caption;
    }
}
