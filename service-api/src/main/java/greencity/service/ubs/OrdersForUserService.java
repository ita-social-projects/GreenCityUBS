package greencity.service.ubs;

import greencity.dto.UserWithOrdersDto;

public interface OrdersForUserService {
    /**
     * Method that return all orders by userId.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Roman Sulymka
     */
    UserWithOrdersDto getAllOrders(Long userId);
}
