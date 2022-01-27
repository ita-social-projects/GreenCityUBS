package greencity.service;

import greencity.ModelUtils;
import greencity.constant.OrderHistory;
import greencity.entity.order.Order;
import greencity.repository.EventRepository;
import greencity.service.ubs.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.save("Замовлення оплаченно", "Анжрій Іванюк", order);
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void changesWithResponsibleEmployeeTest() {
        String existedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.TRUE);
        String unExistedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.FALSE);

        assertEquals(OrderHistory.UPDATE_MANAGER_CALL, existedCallManager);
        assertEquals(OrderHistory.ASSIGN_CALL_MANAGER, unExistedCallManager);
    }
}
