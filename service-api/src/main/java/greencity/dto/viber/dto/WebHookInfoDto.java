package greencity.dto.viber.dto;

import greencity.dto.viber.enums.EventTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WebHookInfoDto {
    private String url;
    private EventTypes[] event_types;
}
