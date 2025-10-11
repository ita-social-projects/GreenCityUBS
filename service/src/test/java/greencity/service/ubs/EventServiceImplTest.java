package greencity.service.ubs;

import static greencity.ModelUtils.getEvent1;
import static greencity.ModelUtils.getEvent2;
import static greencity.ModelUtils.getEventDto;
import static greencity.ModelUtils.getOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.constant.OrderHistory;
import greencity.dto.order.EventDto;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {
    @Mock
    ModelMapper modelMapper;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private EventServiceImpl eventService;

    @Test
    void testEventSave() {
        Order order = getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.save("Замовлення оплаченно", "Анжрій Іванюк", order);
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void saveEmptyEventTest() {
        Order order = getOrder();
        eventService.save("", "admin", order);
        verify(eventRepository, times(0)).save(any());
    }

    @Test
    void testSaveEventEng() {
        String eventAuthorSystem = "Система";
        String eventAuthorClient = "Клієнт";
        Order order = getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.ORDER_FORMED_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_PAID_UK, eventAuthorClient, order);
        eventService.save(OrderHistory.ADD_PAYMENT_SYSTEM_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_ADJUSTMENT_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.ORDER_CONFIRMED_UK, eventAuthorSystem, order);

        assertEquals("Order Status - Formed", OrderHistory.ORDER_FORMED_EN);
        assertEquals("System", OrderHistory.SYSTEM_EN);
        assertEquals("Client", OrderHistory.CLIENT_EN);
        assertEquals("Order Paid", OrderHistory.ORDER_PAID_EN);
        assertEquals("Added payment  №", OrderHistory.ADD_PAYMENT_SYSTEM_EN);
        assertEquals("Added payment  №", OrderHistory.ADD_PAYMENT_SYSTEM_EN);
        assertEquals("Order Status - Approval", OrderHistory.ORDER_ADJUSTMENT_EN);
        assertEquals("Order Status - Confirmed", OrderHistory.ORDER_CONFIRMED_EN);
        verify(eventRepository, times(5)).save(any());
    }

    @Test
    void testGetEventNameEngWithDate() {
        String eventAuthorSystem = "Система";
        Order order = getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.UPDATE_DATE_EXPORT_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.SET_DATE_EXPORT_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.UPDATE_MIX_WASTE_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.ADD_NEW_ECO_NUMBER_UK, eventAuthorSystem, order);
        eventService.save(OrderHistory.DELETED_ECO_NUMBER_UK, eventAuthorSystem, order);

        assertEquals("Змінено деталі вивезення. Дата вивезення:", OrderHistory.UPDATE_DATE_EXPORT_UK);
        assertEquals("Встановлено деталі вивезення. Дата вивезення:", OrderHistory.SET_DATE_EXPORT_UK);
        assertEquals("Змінено деталі замовлення. Мікс відходів ", OrderHistory.UPDATE_MIX_WASTE_UK);
        assertEquals("Додано номер замовлення з магазину", OrderHistory.ADD_NEW_ECO_NUMBER_UK);
        assertEquals("Видалено номер замовлення з магазину", OrderHistory.DELETED_ECO_NUMBER_UK);

        verify(eventRepository, times(5)).save(any());
    }

    @Test
    void testSaveEventEngWithUserName() {
        String userName = "Test";
        Order order = getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.DELETE_PAYMENT_MANUALLY_UK, userName, order);
        eventService.save(OrderHistory.ORDER_BROUGHT_IT_HIMSELF_UK, userName, order);
        eventService.save(OrderHistory.UPDATE_PAYMENT_MANUALLY_UK, userName, order);
        eventService.save(OrderHistory.ORDER_HALF_PAID_UK, userName, order);
        eventService.save(OrderHistory.ADD_PAYMENT_MANUALLY_UK, userName, order);
        eventService.save(OrderHistory.ADD_ADMIN_COMMENT_UK, userName, order);
        eventService.save(OrderHistory.DELETE_VIOLATION_UK, userName, order);

        assertEquals("Payment removed №", OrderHistory.DELETE_PAYMENT_MANUALLY_EN);
        assertEquals("Order status - Will bring it myself", OrderHistory.ORDER_BROUGHT_IT_HIMSELF_EN);
        assertEquals("Payment details changed № ", OrderHistory.UPDATE_PAYMENT_MANUALLY_EN);
        assertEquals("Order partially paid", OrderHistory.ORDER_HALF_PAID_EN);
        assertEquals("Added payment №", OrderHistory.ADD_PAYMENT_MANUALLY_EN);
        assertEquals("Comment added", OrderHistory.ADD_ADMIN_COMMENT_EN);
        assertEquals("Violation removed", OrderHistory.DELETE_VIOLATION_EN);

        verify(eventRepository, times(7)).save(any());
    }

    @Test
    void changesWithResponsibleEmployeeTest() {
        String existedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.TRUE);
        String unExistedCallManager = eventService.changesWithResponsibleEmployee(2L, Boolean.FALSE);

        assertEquals(OrderHistory.UPDATE_MANAGER_CALL_UK, existedCallManager);
        assertEquals(OrderHistory.ASSIGN_CALL_MANAGER_UK, unExistedCallManager);
    }

    @Test
    void testSaveEvent() {
        Order order = getOrder();
        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0),
            ModelUtils.getListOfEvents().get(1)));
        when(employeeRepository.findByEmail(anyString())).thenReturn(Optional.of(ModelUtils.TEST_EMPLOYEE));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));
        eventService.saveEvent("Замовлення оплаченно", "email", order);
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void eventNameToEngMapTest() {
        String eventAuthor = "UBS ADMIN";
        Order order = getOrder();

        order.setEvents(Arrays.asList(ModelUtils.getListOfEvents().get(0), ModelUtils.getListOfEvents().get(1)));
        when(eventRepository.save(any())).thenReturn(ModelUtils.getListOfEvents().get(0));

        eventService.save(OrderHistory.SET_EXPORT_DETAILS_EN, eventAuthor, order);

        assertEquals("Installed export details.", OrderHistory.SET_EXPORT_DETAILS_EN);
        assertEquals("Встановлено деталі вивезення.", OrderHistory.SET_EXPORT_DETAILS_UK);

        verify(eventRepository, times(1)).save(any());
    }

    @Test
    void getAllEventsForOrder_orderNotFound_throwsException() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> eventService.getAllEventsForOrder(orderId, "test@email.com", "uk"));
    }

    @Test
    void getAllEventsForOrder_noEventsFound_throwsException() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));
        when(eventRepository.findAllEventsByOrderId(orderId)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
            () -> eventService.getAllEventsForOrder(orderId, "test@email.com", "uk"));
    }

    @Test
    void getAllEventsForOrder_withEventsUkLanguage_returnsSortedDtos() {
        Long orderId = 1L;
        Order order = getOrder();
        Event event1 = getEvent1();
        event1.setEventDate(LocalDateTime.now().minusDays(1));
        Event event2 = getEvent2();
        event2.setEventDate(LocalDateTime.now());
        List<Event> events = List.of(event1, event2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(eventRepository.findAllEventsByOrderId(orderId)).thenReturn(events);
        when(modelMapper.map(any(Event.class), eq(EventDto.class)))
            .thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                EventDto dto = getEventDto();
                dto.setEventDate(e.getEventDate());
                return dto;
            });

        List<EventDto> result = eventService.getAllEventsForOrder(orderId, "email", "uk");

        assertEquals(2, result.size());
        assertTrue(result.get(0).getEventDate().isAfter(result.get(1).getEventDate()));
    }

    @Test
    void getAllEventsForOrder_withEventsEnLanguage_localizesAndReturnsDtos() {
        Long orderId = 1L;
        Order order = getOrder();
        Event event = getEvent1();
        event.setEventDate(LocalDateTime.now());
        event.setEventNameEn("EnglishName");
        event.setAuthorNameEn("EnglishAuthor");
        List<Event> events = List.of(event);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(eventRepository.findAllEventsByOrderId(orderId)).thenReturn(events);
        when(modelMapper.map(any(Event.class), eq(EventDto.class)))
            .thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                EventDto dto = new EventDto();
                dto.setEventDate(e.getEventDate());
                dto.setEventName(e.getEventNameEn());
                dto.setAuthorName(e.getAuthorNameEn());
                return dto;
            });

        List<EventDto> result = eventService.getAllEventsForOrder(orderId, "email", "en");

        assertEquals(1, result.size());
        assertEquals("EnglishName", result.get(0).getEventName());
        assertEquals("EnglishAuthor", result.get(0).getAuthorName());
    }

    @Test
    void getAllEventsForOrder_withInvalidLanguage_throwsBadRequest() {
        Long orderId = 1L;
        Order order = getOrder();
        Event event = getEvent1();
        event.setEventDate(LocalDateTime.now());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(eventRepository.findAllEventsByOrderId(orderId)).thenReturn(List.of(event));

        assertThrows(BadRequestException.class,
            () -> eventService.getAllEventsForOrder(orderId, "email", "pl"));
    }
}
