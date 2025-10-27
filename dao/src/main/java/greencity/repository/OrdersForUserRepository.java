package greencity.repository;

import greencity.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersForUserRepository extends PagingAndSortingRepository<Order, Long> {
    /**
     * Method gets all orders for user by Id and sorting by column name.
     *
     * @author Roman Sulymka
     */
    Page<Order> getAllOrdersByUserId(Pageable pageable, @Param(value = "userId") Long userId);

    /**
     * Returns an order for the specified user.
     *
     * @param uuid user's uuid.
     * @return {@link Order}.
     */
    Order getAllByUserUuidAndId(String uuid, Long id);
}
