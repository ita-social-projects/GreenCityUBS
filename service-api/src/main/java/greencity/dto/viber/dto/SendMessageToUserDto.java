package greencity.dto.viber.dto;

import greencity.dto.viber.enums.MessageType;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class SendMessageToUserDto {
    private String receiver;
    private MessageType type;
    private SenderDto sender;
    private String text;
    private String context;
}
