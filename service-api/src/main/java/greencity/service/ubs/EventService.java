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

    /**
     * This method return correct status for changes with Responsible employee.
     *
     * @param positionId    ID changed position.
     * @param existedBefore True - We update Info, otherwise we assign a new one
     * @author Rostyslav Sikhovskiy.
     */
    String changesWithResponsibleEmployee(Long positionId, Boolean existedBefore);
}
