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
    void save(String eventName, String eventAuthor, Order order, String eventNameEng, String eventAuthorNameEng);

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
     * @param order {@link Order}
     * @param email {@link String}.
     * @author Hlazova Nataliia.
     */
    void saveEvent(String name, String email, Order order, String nameEng);
}
