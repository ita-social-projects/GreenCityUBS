package greencity.service.ubs;

import greencity.dto.order.EventDto;
import java.util.List;

public interface EventService {
    /**
     * This is method which collect's information about order history lifecycle.
     *
     * @param eventName   String.
     * @param eventAuthor String.
     * @param orderId     Long.
     * @author Yuriy Bahlay.
     */
    void save(String eventName, String eventAuthor, Long orderId);

    /**
     * This method return correct status for changes with Responsible employee.
     *
     * @param positionId    ID changed position.
     * @param existedBefore True - We update Info, otherwise we assign a new one
     * @author Rostyslav Sikhovskiy.
     */
    String changesWithResponsibleEmployee(Long positionId, Boolean existedBefore);

    /**
     * Method save event with employee.
     *
     * @param name  {@link String};
     * @param orderId {@link Long};
     * @param email {@link String}.
     * @author Hlazova Nataliia.
     */
    void saveEvent(String name, String email, Long orderId);

    /**
     * Methods for finding all events for Order.
     *
     * @param orderId  {@link Long} id.
     * @param email    {@link String};
     * @param language {@link String};
     * @return {@link List} that contains list of EventsDTOS.
     * @author Yuriy Bahlay.
     */
    List<EventDto> getAllEventsForOrder(Long orderId, String email, String language);
}
