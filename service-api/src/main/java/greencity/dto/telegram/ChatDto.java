package greencity.dto.telegram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private Long id;
    private String chatId;
    private String firstName;
    private String lastName;
    private String username;
    private ChatUserDto user;
    private TelegramMessageDto lastMessage;
}
