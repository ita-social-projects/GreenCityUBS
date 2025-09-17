package greencity.dto.telegram;

import greencity.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotResponseDto {
    private Long id;
    private MessageType messageType;
    private String lang;
    private String text;
}
