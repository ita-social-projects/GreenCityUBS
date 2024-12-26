package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.OrderHistory;
import greencity.entity.order.Order;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;

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
        String eventAuthorSystem = "Система";
        String eventAuthorClient = "Клієнт";
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.ORDER_FORMED, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_PAID, eventAuthorClient, order);
        eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_ADJUSTMENT, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_CONFIRMED, eventAuthorSystem, order);

        assertEquals("Order Status - Formed", OrderHistory.ORDER_FORMED_ENG);
        assertEquals("System", OrderHistory.SYSTEM_ENG);
        assertEquals("Client", OrderHistory.CLIENT_ENG);
        assertEquals("Order Paid", OrderHistory.ORDER_PAID_ENG);
        assertEquals("Added payment  №", OrderHistory.ADD_PAYMENT_SYSTEM_ENG);
        assertEquals("Added payment  №", OrderHistory.ADD_PAYMENT_SYSTEM_ENG);
        assertEquals("Order Status - Approval", OrderHistory.ORDER_ADJUSTMENT_ENG);
        assertEquals("Order Status - Confirmed", OrderHistory.ORDER_CONFIRMED_ENG);
        verify(eventRepository, times(5)).save(any());
    }

    @Test
    void testGetEventNameEngWithDate() {
        String eventAuthorSystem = "Система";
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.UPDATE_DATE_EXPORT, eventAuthorSystem, order);
        eventService.save(OrderHistory.SET_DATE_EXPORT, eventAuthorSystem, order);
        eventService.save(OrderHistory.UPDATE_MIX_WASTE, eventAuthorSystem, order);
        eventService.save(OrderHistory.ADD_NEW_ECO_NUMBER, eventAuthorSystem, order);
        eventService.save(OrderHistory.DELETED_ECO_NUMBER, eventAuthorSystem, order);

        assertEquals("Змінено деталі вивезення. Дата вивезення:", OrderHistory.UPDATE_DATE_EXPORT);
        assertEquals("Встановлено деталі вивезення. Дата вивезення:", OrderHistory.SET_DATE_EXPORT);
        assertEquals("Змінено деталі замовлення. Мікс відходів ", OrderHistory.UPDATE_MIX_WASTE);
        assertEquals("Додано номер замовлення з магазину", OrderHistory.ADD_NEW_ECO_NUMBER);
        assertEquals("Видалено номер замовлення з магазину", OrderHistory.DELETED_ECO_NUMBER);

        verify(eventRepository, times(5)).save(any());
    }

    @Test
    void testSaveEventEngWithUserName() {
        String userName = "Test";
        Order order = ModelUtils.getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.DELETE_PAYMENT_MANUALLY, userName, order);
        eventService.save(OrderHistory.ORDER_BROUGHT_IT_HIMSELF, userName, order);
        eventService.save(OrderHistory.UPDATE_PAYMENT_MANUALLY, userName, order);
        eventService.save(OrderHistory.ORDER_HALF_PAID, userName, order);
        eventService.save(OrderHistory.ADD_PAYMENT_MANUALLY, userName, order);
        eventService.save(OrderHistory.ADD_ADMIN_COMMENT, userName, order);
        eventService.save(OrderHistory.DELETE_VIOLATION, userName, order);

        assertEquals("Payment removed №", OrderHistory.DELETE_PAYMENT_MANUALLY_ENG);
        assertEquals("Order status - Will bring it myself", OrderHistory.ORDER_BROUGHT_IT_HIMSELF_ENG);
        assertEquals("Payment details changed № ", OrderHistory.UPDATE_PAYMENT_MANUALLY_ENG);
        assertEquals("Order partially paid", OrderHistory.ORDER_HALF_PAID_ENG);
        assertEquals("Added payment №", OrderHistory.ADD_PAYMENT_MANUALLY_ENG);
        assertEquals("Comment added", OrderHistory.ADD_ADMIN_COMMENT_ENG);
        assertEquals("Violation removed", OrderHistory.DELETE_VIOLATION_ENG);

        verify(eventRepository, times(7)).save(any());
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
