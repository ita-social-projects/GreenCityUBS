package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.OrderHistory;
import greencity.entity.order.Order;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.service.ubs.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testEventSave() {
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.save("Замовлення оплаченно", "Анжрій Іванюк", order,
            "Order paid", "Анжрій Іванюк");
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void saveEmptyEventTest() {
        Order order = ModelUtils.getOrder();
        eventService.save("", "admin", order, "", "admin");
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void changesWithResponsibleEmployeeTest() {
        String existedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.TRUE);
        String unExistedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.FALSE);

        assertEquals(OrderHistory.UPDATE_MANAGER_CALL, existedCallManager);
        assertEquals(OrderHistory.ASSIGN_CALL_MANAGER, unExistedCallManager);
    }

    @Test
    void testSaveEvent() {
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(ModelUtils.TEST_EMPLOYEE));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.saveEvent("Замовлення оплаченно", "email", order, "The order is paid");
        verify(eventRepository, times(1)).save(any());
    }
}
