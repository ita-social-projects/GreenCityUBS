package greencity.dto.viber.dto;

import greencity.dto.viber.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
