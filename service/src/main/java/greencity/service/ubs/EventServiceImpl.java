package greencity.service.ubs;

import greencity.constant.OrderHistory;
import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

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
        if (existedBefore.equals(Boolean.TRUE)) {
            if (positionId == 2) {
                return OrderHistory.UPDATE_MANAGER_CALL;
            } else if (positionId == 3) {
                return OrderHistory.UPDATE_MANAGER_LOGIEST;
            } else if (positionId == 4) {
                return OrderHistory.UPDATE_MANAGER_CALL_PILOT;
            } else if (positionId == 5) {
                return OrderHistory.UPDATE_MANAGER_DRIVER;
            }
        } else {
            if (positionId == 2) {
                return OrderHistory.ASSIGN_CALL_MANAGER;
            } else if (positionId == 3) {
                return OrderHistory.ASSIGN_LOGIEST;
            } else if (positionId == 4) {
                return OrderHistory.ASSIGN_CALL_PILOT;
            } else if (positionId == 5) {
                return OrderHistory.ASSIGN_DRIVER;
            }
        }
        return "Немає відповідної позиції";
    }
}
