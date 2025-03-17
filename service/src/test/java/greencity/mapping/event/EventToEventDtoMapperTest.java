package greencity.mapping.event;

import greencity.ModelUtils;
import greencity.dto.order.EventDto;
import greencity.entity.order.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EventToEventDtoMapperTest {
    @InjectMocks
    private EventToEventDtoMapper mapper;

    @Test
    void convertTest() {
        Event event = ModelUtils.getEvent3();
        EventDto expectedEventDto = ModelUtils.getEventDto();
        EventDto actualEventDto = mapper.convert(event);
        assertEquals(actualEventDto.getEventDate(), expectedEventDto.getEventDate());
        assertEquals(actualEventDto.getEventName(), expectedEventDto.getEventName());
        assertEquals(actualEventDto.getId(), expectedEventDto.getId());
        assertEquals(actualEventDto.getAuthorName(), expectedEventDto.getAuthorName());
    }
}
