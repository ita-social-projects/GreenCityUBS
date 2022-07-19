package greencity.service.ubs;

import greencity.constant.OrderHistory;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.entity.user.employee.Employee;
import greencity.exceptions.NotFoundException;
import greencity.repository.EmployeeRepository;
import greencity.repository.EventRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        Event event = new Event();
        event.setEventDate(LocalDateTime.now());
        event.setEventName(eventName);
        event.setAuthorName(eventAuthor);
        if (order.getEvents() != null) {
            List<Event> events = new ArrayList<>(order.getEvents());
            events.add(event);
            order.setEvents(events);
        }
        event.setOrder(order);
        eventRepository.save(event);
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

    @RequiredArgsConstructor
    @Getter
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
