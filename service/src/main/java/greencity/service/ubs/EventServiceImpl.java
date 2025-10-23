package greencity.service.ubs;

import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND_BY_EMAIL;
import static greencity.constant.ErrorMessage.EVENTS_NOT_FOUND_EXCEPTION;
import static greencity.constant.ErrorMessage.ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND_BY_ID;
import static greencity.constant.ErrorMessage.ORDER_NOT_FOUND_BY_ID;
import greencity.constant.AppConstant;
import greencity.constant.OrderHistory;
import greencity.dto.order.EventDto;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import greencity.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    /**
     * This is method which collect's information about order history lifecycle.
     *
     * @param eventName   String.
     * @param eventAuthor String.
     * @param orderId     Long.
     * @author Yuriy Bahlay.
     */
    public void save(String eventName, String eventAuthor, Long orderId) {
        if (eventName.isEmpty()) {
            return;
        }
        Event event = new Event();
        event.setEventDate(LocalDateTime.now());
        event.setEventNameUk(eventName);
        event.setAuthorNameUk(eventAuthor);
        event.setEventNameEn(getEventNameEng(eventName));
        event.setAuthorNameEn(getAuthorNameEng(eventAuthor));
        getEventNameEngWithNumbers(eventName, event);
        getEventNameEngWithDate(eventName, event);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND_BY_ID + orderId));
        if (order.getEvents() != null) {
            List<Event> events = new ArrayList<>(order.getEvents());
            events.add(event);
            order.setEvents(events);
        }
        event.setOrder(order);
        eventRepository.save(event);
    }

    private void getEventNameEngWithDate(String eventName, Event event) {
        if (eventName.startsWith(OrderHistory.UPDATE_DATE_EXPORT_UK)) {
            event.setEventNameEn(
                OrderHistory.UPDATE_EXPORT_DETAILS_EN + String.format(OrderHistory.UPDATE_EXPORT_DATA_EN,
                    eventName.substring(OrderHistory.UPDATE_DATE_EXPORT_UK.length())));
        } else if (eventName.startsWith(OrderHistory.SET_DATE_EXPORT_UK)) {
            event.setEventNameEn(
                OrderHistory.SET_EXPORT_DETAILS_EN + String.format(OrderHistory.UPDATE_EXPORT_DATA_EN,
                    eventName.substring(OrderHistory.SET_DATE_EXPORT_UK.length())));
        } else if (eventName.startsWith(OrderHistory.UPDATE_MIX_WASTE_UK)) {
            event.setEventNameEn(
                OrderHistory.SET_EXPORT_DETAILS_EN + String.format(OrderHistory.UPDATE_ORDER_EXPORT_EN,
                    eventName.substring(OrderHistory.UPDATE_MIX_WASTE_UK.length())));
        }
    }

    private void getEventNameEngWithNumbers(String eventName, Event event) {
        if (eventName.startsWith(OrderHistory.ADD_PAYMENT_SYSTEM_UK)) {
            event.setEventNameEn(
                OrderHistory.ADD_PAYMENT_SYSTEM_EN + eventName.substring(OrderHistory.ADD_PAYMENT_SYSTEM_UK.length()));
        } else if (eventName.startsWith(OrderHistory.DELETE_PAYMENT_MANUALLY_UK)) {
            event.setEventNameEn(OrderHistory.DELETE_PAYMENT_MANUALLY_EN
                + eventName.substring(OrderHistory.DELETE_PAYMENT_MANUALLY_UK.length()));
        } else if (eventName.startsWith(OrderHistory.UPDATE_PAYMENT_MANUALLY_UK)) {
            event.setEventNameEn(OrderHistory.UPDATE_PAYMENT_MANUALLY_EN
                + eventName.substring(OrderHistory.UPDATE_PAYMENT_MANUALLY_UK.length()));
        } else if (eventName.startsWith(OrderHistory.ADD_PAYMENT_MANUALLY_UK)) {
            event.setEventNameEn(OrderHistory.ADD_PAYMENT_MANUALLY_EN
                + eventName.substring(OrderHistory.ADD_PAYMENT_MANUALLY_UK.length()));
        } else if (eventName.startsWith(OrderHistory.ADD_NEW_ECO_NUMBER_UK)) {
            event.setEventNameEn(OrderHistory.ADD_NEW_ECO_NUMBER_EN
                + eventName.substring(OrderHistory.ADD_NEW_ECO_NUMBER_UK.length()));
        } else if (eventName.startsWith(OrderHistory.DELETED_ECO_NUMBER_UK)) {
            event.setEventNameEn(OrderHistory.DELETED_ECO_NUMBER_EN
                + eventName.substring(OrderHistory.DELETED_ECO_NUMBER_UK.length()));
        }
    }

    private static final Map<String, String> eventNameToEngMap = new HashMap<>();

    static {
        eventNameToEngMap.put(OrderHistory.ORDER_FORMED_UK, OrderHistory.ORDER_FORMED_EN);
        eventNameToEngMap.put(OrderHistory.ASSIGN_DRIVER_UK, OrderHistory.ASSIGN_DRIVER_EN);
        eventNameToEngMap.put(OrderHistory.ASSIGN_LOGIEST_UK, OrderHistory.ASSIGN_LOGIEST_EN);
        eventNameToEngMap.put(OrderHistory.ASSIGN_CALL_MANAGER_UK, OrderHistory.ASSIGN_CALL_MANAGER_EN);
        eventNameToEngMap.put(OrderHistory.ASSIGN_CALL_PILOT_UK, OrderHistory.ASSIGN_CALL_PILOT_EN);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_CALL_UK, OrderHistory.UPDATE_MANAGER_CALL_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_ON_THE_ROUTE_UK, OrderHistory.ORDER_ON_THE_ROUTE_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_DONE_UK, OrderHistory.ORDER_DONE_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_CANCELLED_UK, OrderHistory.ORDER_CANCELLED_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_NOT_TAKEN_OUT_UK, OrderHistory.ORDER_NOT_TAKEN_OUT_EN);
        eventNameToEngMap.put(OrderHistory.ADD_VIOLATION_UK, OrderHistory.ADD_VIOLATION_EN);
        eventNameToEngMap.put(OrderHistory.CHANGES_VIOLATION_UK, OrderHistory.CHANGES_VIOLATION_EN);
        eventNameToEngMap.put(OrderHistory.ADDED_BONUSES_UK, OrderHistory.ADDED_BONUSES_EN);
        eventNameToEngMap.put(OrderHistory.CHANGED_SENDER_UK, OrderHistory.CHANGED_SENDER_EN);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_LOGIEST_UK, OrderHistory.UPDATE_MANAGER_LOGIEST_EN);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_CALL_PILOT_UK, OrderHistory.UPDATE_MANAGER_CALL_PILOT_EN);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_DRIVER_UK, OrderHistory.UPDATE_MANAGER_DRIVER_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_PAID_UK, OrderHistory.ORDER_PAID_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_ADJUSTMENT_UK, OrderHistory.ORDER_ADJUSTMENT_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_BROUGHT_IT_HIMSELF_UK, OrderHistory.ORDER_BROUGHT_IT_HIMSELF_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_CONFIRMED_UK, OrderHistory.ORDER_CONFIRMED_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_HALF_PAID_UK, OrderHistory.ORDER_HALF_PAID_EN);
        eventNameToEngMap.put(OrderHistory.ORDER_STATUS_UPDATED_UK, OrderHistory.ORDER_STATUS_UPDATED_EN);
        eventNameToEngMap.put(OrderHistory.ADD_ADMIN_COMMENT_UK, OrderHistory.ADD_ADMIN_COMMENT_EN);
        eventNameToEngMap.put(OrderHistory.DELETE_VIOLATION_UK, OrderHistory.DELETE_VIOLATION_EN);
        eventNameToEngMap.put(OrderHistory.CANCELED_ORDER_MONEY_REFUND_UK, OrderHistory.CANCELED_ORDER_MONEY_REFUND_EN);
        eventNameToEngMap.put(OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE_UK,
            OrderHistory.WASTE_REMOVAL_ADDRESS_CHANGE_EN);
        eventNameToEngMap.put(OrderHistory.SET_EXPORT_DETAILS_UK, OrderHistory.SET_EXPORT_DETAILS_EN);
    }

    private static String getEventNameEng(String eventName) {
        return eventNameToEngMap.getOrDefault(eventName, eventName);
    }

    private static String getAuthorNameEng(String eventAuthor) {
        if (OrderHistory.SYSTEM_UK.equals(eventAuthor)) {
            return OrderHistory.SYSTEM_EN;
        } else if (OrderHistory.CLIENT_UK.equals(eventAuthor)) {
            return OrderHistory.CLIENT_EN;
        } else {
            return eventAuthor;
        }
    }

    /**
     * This method return correct status for changes with Responsible employee.
     *
     * @param positionId    ID changed position.
     * @param existedBefore If True - We update Info, otherwise we assign
     * @author Rostyslav Sikhovskiy.
     */
    @Override
    public String changesWithResponsibleEmployee(Long positionId, Boolean existedBefore) {
        EmployeePositionChanges employeePosition = EmployeePositionChanges.fromEmployeePosition(positionId);
        return existedBefore == Boolean.TRUE ? employeePosition.getUpdated() : employeePosition.getAssigned();
    }

    /**
     * Method save event with employee.
     *
     * @param name    {@link String};
     * @param orderId {@link Long}
     * @param email   {@link String}.
     * @author Hlazova Nataliia.
     */
    @Override
    public void saveEvent(String name, String email, Long orderId) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND_BY_EMAIL + email));
        save(name, employee.getFirstName()
            + "  " + employee.getLastName(), orderId);
    }

    @Override
    public List<EventDto> getAllEventsForOrder(Long orderId, String email, String language) {
        Optional<Order> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            throw new NotFoundException(ORDER_WITH_CURRENT_ID_DOES_NOT_EXIST + orderId);
        }

        List<Event> orderEvents = eventRepository.findAllEventsByOrderId(orderId);
        if (orderEvents.isEmpty()) {
            throw new NotFoundException(EVENTS_NOT_FOUND_EXCEPTION + orderId);
        }

        localizeEventNames(orderEvents, language);
        return orderEvents.stream()
            .map(event -> modelMapper.map(event, EventDto.class))
            .sorted(Comparator.comparing(EventDto::getEventDate).reversed())
            .toList();
    }

    @Data
    private static class EmployeePositionChanges {
        private final String updated;
        private final String assigned;
        private final Long position;

        static final EmployeePositionChanges CALLER_MANAGER =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_CALL_UK,
                OrderHistory.ASSIGN_CALL_MANAGER_UK, 2L);
        static final EmployeePositionChanges LOGIC_MAN =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_LOGIEST_UK,
                OrderHistory.ASSIGN_LOGIEST_UK, 3L);
        static final EmployeePositionChanges NAVIGATOR =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_CALL_PILOT_UK,
                OrderHistory.ASSIGN_CALL_PILOT_UK, 4L);
        static final EmployeePositionChanges DRIVER = new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_DRIVER_UK,
            OrderHistory.ASSIGN_DRIVER_UK, 5L);

        static final Map<Long, EmployeePositionChanges> ALL_VAlUES =
            Stream.of(CALLER_MANAGER, LOGIC_MAN, NAVIGATOR, DRIVER)
                .collect(Collectors.toMap(EmployeePositionChanges::getPosition, Function.identity()));

        public static EmployeePositionChanges fromEmployeePosition(Long position) {
            return Optional.ofNullable(ALL_VAlUES.get(position))
                .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND_BY_ID + position));
        }
    }

    /**
     * Method that takes a list of events and a language and localizes the event
     * names and author names in the list of events.
     *
     * @param events   a list of events
     * @param language a language
     */
    private void localizeEventNames(List<Event> events, String language) {
        if (AppConstant.LANGUAGE_EN.equals(language)) {
            events.forEach(event -> {
                event.setEventNameUk(event.getEventNameEn());
                event.setAuthorNameUk(event.getAuthorNameEn());
            });
        } else if (!AppConstant.LANGUAGE_UK.equals(language)) {
            throw new BadRequestException("Unexpected value: " + language);
        }
    }
}
