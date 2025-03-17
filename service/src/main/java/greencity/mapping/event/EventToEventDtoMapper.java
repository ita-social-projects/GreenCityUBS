package greencity.mapping.event;

import greencity.dto.order.EventDto;
import greencity.entity.order.Event;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventToEventDtoMapper extends AbstractConverter<Event, EventDto> {
    @Override
    protected EventDto convert(Event event) {
        return EventDto.builder()
            .eventDate(event.getEventDate())
            .id(event.getId())
            .eventName(event.getEventNameUk())
            .authorName(event.getAuthorNameUk())
            .build();
    }
}
