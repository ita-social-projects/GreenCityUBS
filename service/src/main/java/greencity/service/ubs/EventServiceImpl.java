package greencity.service.ubs;

import greencity.constant.OrderHistory;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static greencity.constant.ErrorMessage.EMPLOYEE_NOT_FOUND;
import static greencity.constant.ErrorMessage.POSITION_NOT_FOUND_BY_ID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * This is method which collect's information about order history lifecycle.
     *
     * @param eventName   String.
     * @param eventAuthor String.
     * @param order       Order.
     * @author Yuriy Bahlay.
     */
    public void save(String eventName, String eventAuthor, Order order) {
        if (eventName.isEmpty()) {
            return;
        }
        Event event = new Event();
        event.setEventDate(LocalDateTime.now());
        event.setEventName(eventName);
        event.setAuthorName(eventAuthor);
        event.setEventNameEng(getEventNameEng(eventName));
        event.setAuthorNameEng(getAuthorNameEng(eventAuthor));
        getEventNameEngWithNumbers(eventName, event);
        getEventNameEngWithDate(eventName, event);

        if (order.getEvents() != null) {
            List<Event> events = new ArrayList<>(order.getEvents());
            events.add(event);
            order.setEvents(events);
        }
        event.setOrder(order);
        eventRepository.save(event);
    }

    private void getEventNameEngWithDate(String eventName, Event event) {
        if (eventName.startsWith(OrderHistory.UPDATE_DATE_EXPORT)) {
            event.setEventNameEng(
                OrderHistory.UPDATE_EXPORT_DETAILS_ENG + String.format(OrderHistory.UPDATE_EXPORT_DATA_ENG,
                    eventName.substring(OrderHistory.UPDATE_DATE_EXPORT.length())));
        } else if (eventName.startsWith(OrderHistory.SET_DATE_EXPORT)) {
            event.setEventNameEng(
                OrderHistory.SET_EXPORT_DETAILS_ENG + String.format(OrderHistory.UPDATE_EXPORT_DATA_ENG,
                    eventName.substring(OrderHistory.SET_DATE_EXPORT.length())));
        } else if (eventName.startsWith(OrderHistory.UPDATE_MIX_WASTE)) {
            event.setEventNameEng(
                OrderHistory.SET_EXPORT_DETAILS_ENG + String.format(OrderHistory.UPDATE_ORDER_EXPORT_ENG,
                    eventName.substring(OrderHistory.UPDATE_MIX_WASTE.length())));
        }
    }

    private void getEventNameEngWithNumbers(String eventName, Event event) {
        if (eventName.startsWith(OrderHistory.ADD_PAYMENT_SYSTEM)) {
            event.setEventNameEng(
                OrderHistory.ADD_PAYMENT_SYSTEM_ENG + eventName.substring(OrderHistory.ADD_PAYMENT_SYSTEM.length()));
        } else if (eventName.startsWith(OrderHistory.DELETE_PAYMENT_MANUALLY)) {
            event.setEventNameEng(OrderHistory.DELETE_PAYMENT_MANUALLY_ENG
                + eventName.substring(OrderHistory.DELETE_PAYMENT_MANUALLY.length()));
        } else if (eventName.startsWith(OrderHistory.UPDATE_PAYMENT_MANUALLY)) {
            event.setEventNameEng(OrderHistory.UPDATE_PAYMENT_MANUALLY_ENG
                + eventName.substring(OrderHistory.UPDATE_PAYMENT_MANUALLY.length()));
        } else if (eventName.startsWith(OrderHistory.ADD_PAYMENT_MANUALLY)) {
            event.setEventNameEng(OrderHistory.ADD_PAYMENT_MANUALLY_ENG
                + eventName.substring(OrderHistory.ADD_PAYMENT_MANUALLY.length()));
        } else if (eventName.startsWith(OrderHistory.ADD_NEW_ECO_NUMBER)) {
            event.setEventNameEng(OrderHistory.ADD_NEW_ECO_NUMBER_ENG
                + eventName.substring(OrderHistory.ADD_NEW_ECO_NUMBER.length()));
        } else if (eventName.startsWith(OrderHistory.DELETED_ECO_NUMBER)) {
            event.setEventNameEng(OrderHistory.DELETED_ECO_NUMBER_ENG
                + eventName.substring(OrderHistory.DELETED_ECO_NUMBER.length()));
        }
    }

    private static final Map<String, String> eventNameToEngMap = new HashMap<>();

    static {
        eventNameToEngMap.put(OrderHistory.ORDER_FORMED, OrderHistory.ORDER_FORMED_ENG);
        eventNameToEngMap.put(OrderHistory.ASSIGN_DRIVER, OrderHistory.ASSIGN_DRIVER_ENG);
        eventNameToEngMap.put(OrderHistory.ASSIGN_LOGIEST, OrderHistory.ASSIGN_LOGIEST_ENG);
        eventNameToEngMap.put(OrderHistory.ASSIGN_CALL_MANAGER, OrderHistory.ASSIGN_CALL_MANAGER_ENG);
        eventNameToEngMap.put(OrderHistory.ASSIGN_CALL_PILOT, OrderHistory.ASSIGN_CALL_PILOT_ENG);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_CALL, OrderHistory.UPDATE_MANAGER_CALL_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_ON_THE_ROUTE, OrderHistory.ORDER_ON_THE_ROUTE_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_DONE, OrderHistory.ORDER_DONE_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_CANCELLED, OrderHistory.ORDER_CANCELLED_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_NOT_TAKEN_OUT, OrderHistory.ORDER_NOT_TAKEN_OUT_ENG);
        eventNameToEngMap.put(OrderHistory.ADD_VIOLATION, OrderHistory.ADD_VIOLATION_ENG);
        eventNameToEngMap.put(OrderHistory.CHANGES_VIOLATION, OrderHistory.CHANGES_VIOLATION_ENG);
        eventNameToEngMap.put(OrderHistory.ADDED_BONUSES, OrderHistory.ADDED_BONUSES_ENG);
        eventNameToEngMap.put(OrderHistory.CHANGED_SENDER, OrderHistory.CHANGED_SENDER_ENG);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_LOGIEST, OrderHistory.UPDATE_MANAGER_LOGIEST_ENG);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_CALL_PILOT, OrderHistory.UPDATE_MANAGER_CALL_PILOT_ENG);
        eventNameToEngMap.put(OrderHistory.UPDATE_MANAGER_DRIVER, OrderHistory.UPDATE_MANAGER_DRIVER_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_PAID, OrderHistory.ORDER_PAID_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_ADJUSTMENT, OrderHistory.ORDER_ADJUSTMENT_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_BROUGHT_IT_HIMSELF, OrderHistory.ORDER_BROUGHT_IT_HIMSELF_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_CONFIRMED, OrderHistory.ORDER_CONFIRMED_ENG);
        eventNameToEngMap.put(OrderHistory.ORDER_HALF_PAID, OrderHistory.ORDER_HALF_PAID_ENG);
        eventNameToEngMap.put(OrderHistory.ADD_ADMIN_COMMENT, OrderHistory.ADD_ADMIN_COMMENT_ENG);
        eventNameToEngMap.put(OrderHistory.DELETE_VIOLATION, OrderHistory.DELETE_VIOLATION_ENG);
    }

    private static String getEventNameEng(String eventName) {
        return eventNameToEngMap.getOrDefault(eventName, eventName);
    }

    private static String getAuthorNameEng(String eventAuthor) {
        if (OrderHistory.SYSTEM.equals(eventAuthor)) {
            return OrderHistory.SYSTEM_ENG;
        } else if (OrderHistory.CLIENT.equals(eventAuthor)) {
            return OrderHistory.CLIENT_ENG;
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
     * @param name  {@link String};
     * @param order {@link Order}
     * @param email {@link String}.
     * @author Hlazova Nataliia.
     */
    @Override
    public void saveEvent(String name, String email, Order order) {
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException(EMPLOYEE_NOT_FOUND));
        save(name, employee.getFirstName()
            + "  " + employee.getLastName(), order);
    }

    @Data
    private static class EmployeePositionChanges {
        private final String updated;
        private final String assigned;
        private final Long position;

        static final EmployeePositionChanges CALLER_MANAGER =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_CALL,
                OrderHistory.ASSIGN_CALL_MANAGER, 2L);
        static final EmployeePositionChanges LOGIC_MAN =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_LOGIEST,
                OrderHistory.ASSIGN_LOGIEST, 3L);
        static final EmployeePositionChanges NAVIGATOR =
            new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_CALL_PILOT,
                OrderHistory.ASSIGN_CALL_PILOT, 4L);
        static final EmployeePositionChanges DRIVER = new EmployeePositionChanges(OrderHistory.UPDATE_MANAGER_DRIVER,
            OrderHistory.ASSIGN_DRIVER, 5L);

        static final Map<Long, EmployeePositionChanges> ALL_VAlUES =
            Stream.of(CALLER_MANAGER, LOGIC_MAN, NAVIGATOR, DRIVER)
                .collect(Collectors.toMap(EmployeePositionChanges::getPosition, Function.identity()));

        public static EmployeePositionChanges fromEmployeePosition(Long position) {
            return Optional.ofNullable(ALL_VAlUES.get(position))
                .orElseThrow(() -> new NotFoundException(POSITION_NOT_FOUND_BY_ID + position));
        }
    }
}
