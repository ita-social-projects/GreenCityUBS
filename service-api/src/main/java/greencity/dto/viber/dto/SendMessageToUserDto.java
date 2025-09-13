package greencity.dto.viber.dto;

import greencity.dto.viber.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

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
