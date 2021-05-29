package greencity.dto.viber.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CallbackDto {
    private String event;
    private String timestamp;
    private String type;
    private String context;
    private SenderDto sender;
    private MessageDto message;
    private UserDto user;
    private String subscribed;
}
