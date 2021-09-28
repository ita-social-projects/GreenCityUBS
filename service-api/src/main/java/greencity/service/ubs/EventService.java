package greencity.service.ubs;

import greencity.entity.order.Order;

public interface EventService {
    /**
     * This is method which collect's information about order history lifecycle.
     *
     * @param eventName   String.
     * @param eventAuthor String.
     * @param order       Order.
     * @author Yuriy Bahlay.
     */
    void save(String eventName, String eventAuthor, Order order);
}
