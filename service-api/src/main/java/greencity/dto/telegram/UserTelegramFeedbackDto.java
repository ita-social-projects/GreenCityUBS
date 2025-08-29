package greencity.dto.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserTelegramFeedbackDto {
    private String chatId;
    private String name;
    private int rating;
    private String comment;
    private String subject;
}
