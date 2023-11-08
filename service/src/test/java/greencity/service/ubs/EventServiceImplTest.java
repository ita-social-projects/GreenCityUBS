package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.OrderHistory;
import greencity.entity.order.Order;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.service.ubs.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

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
        eventService.save("Замовлення оплаченно", "Анжрій Іванюк", order);
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void saveEmptyEventTest() {
        Order order = ModelUtils.getOrder();
        eventService.save("", "admin", order);
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void testSaveEventEng() {
        String orderFormed = OrderHistory.ORDER_FORMED;
        String orderPaid = OrderHistory.ORDER_PAID;
        String addPaymentSystem = OrderHistory.ADD_PAYMENT_SYSTEM;
        String orderAdjustment = OrderHistory.ORDER_ADJUSTMENT;
        String orderBroughtItHimself = OrderHistory.ORDER_BROUGHT_IT_HIMSELF;
        String eventAuthor = "Test";
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(orderFormed, eventAuthor, order);
        eventService.save(orderPaid, eventAuthor, order);
        eventService.save(addPaymentSystem, eventAuthor, order);
        eventService.save(orderAdjustment, eventAuthor, order);
        eventService.save(orderBroughtItHimself, eventAuthor, order);

        assertEquals(OrderHistory.ORDER_FORMED_ENG, "Order Status - Formed");
        assertEquals(OrderHistory.CLIENT_ENG, "Client");
        assertEquals(OrderHistory.ORDER_PAID_ENG, "Order Paid");
        assertEquals(OrderHistory.SYSTEM_ENG, "System");
        assertEquals(OrderHistory.ADD_PAYMENT_SYSTEM_ENG, "Added payment");
        assertEquals(OrderHistory.ORDER_ADJUSTMENT_ENG, "Order Status - Approval");
        assertEquals(OrderHistory.ORDER_BROUGHT_IT_HIMSELF_ENG, "Order status - Will bring it myself");
        verify(eventRepository, times(5)).save(any());
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
        eventService.saveEvent("Замовлення оплаченно", "email", order);
        verify(eventRepository, times(1)).save(any());
    }
}
