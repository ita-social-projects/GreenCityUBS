package greencity.service.ubs;

import greencity.entity.order.Event;
import greencity.entity.order.Order;
import greencity.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
}
