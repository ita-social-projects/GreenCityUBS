package greencity.dto.viber.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.dto.viber.enums.EventTypes;
import lombok.Builder;
import lombok.ToString;
import java.util.Set;

@Builder
@ToString
public class WebhookDto {
    @JsonProperty("url")
    private String url;
    @JsonProperty("event_types")
    private Set<EventTypes> eventTypes;
}
