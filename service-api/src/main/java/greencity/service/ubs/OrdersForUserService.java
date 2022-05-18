package greencity.service.ubs;

import greencity.dto.order.UserWithOrdersDto;
import greencity.entity.enums.SortingOrder;
import org.springframework.data.domain.Pageable;

public interface OrdersForUserService {
    /**
     * Method that return all orders by userId.
     *
     * @param userId of {@link Long} administrator's user id;
     * @author Roman Sulymka
     */
    UserWithOrdersDto getAllOrders(Pageable page, Long userId, SortingOrder sortType, String column);
}
