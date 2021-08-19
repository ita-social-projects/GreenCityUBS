package greencity.dto.viber.dto;

import greencity.dto.viber.enums.MessageType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class MessageDto {
    private MessageType type;
    private String text;
    private String media;
}
