package greencity.service;

import greencity.ModelUtils;
import greencity.entity.order.Order;
import greencity.repository.EventRepository;
import greencity.service.ubs.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testEventSave() {
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0), ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.save("Замовлення оплаченно", "Анжрій Іванюк", order);
        verify(eventRepository, times(1)).save(any());
    }
}
