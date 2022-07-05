package greencity.repository;

import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersForUserRepository extends PagingAndSortingRepository<Order, Long> {
    /**
     * Method gets all orders for user by Id and sorting by column name.
     *
     * @author Roman Sulymka
     */
    Page<Order> getAllOrdersByUserId(Pageable pageable, @Param(value = "userId") Long userId);

    /**
     * Method gets all orders for user by id and sorting by amount column DESC.
     *
     * @author Roman Sulymka
     */
    @Query(value = "SELECT * FROM orders"
        + "    INNER JOIN payment on orders.id = payment.order_id"
        + "    INNER JOIN users ON orders.users_id = users.id"
        + "    INNER JOIN ubs_user on orders.ubs_user_id = ubs_user.id"
        + "    WHERE orders.users_id = :userId order by payment.amount desc", nativeQuery = true)
    Page<Order> getAllOrdersByUserIdAndAmountDesc(Pageable pageable, @Param(value = "userId") Long userId);

    /**
     * Method gets all orders for user id by and sorting by amount column ASC.
     *
     * @author Roman Sulymka
     */
    @Query(value = "SELECT * FROM orders"
        + "    INNER JOIN payment on orders.id = payment.order_id"
        + "    INNER JOIN users ON orders.users_id = users.id"
        + "    INNER JOIN ubs_user on orders.ubs_user_id = ubs_user.id"
        + "    WHERE orders.users_id = :userId order by payment.amount asc", nativeQuery = true)
    Page<Order> getAllOrdersByUserIdAndAmountASC(Pageable pageable, @Param(value = "userId") Long userId);

    /**
     * Returns a page with orders for the specified user.
     *
     * @param pageable {@link Pageable} page to return.
     * @param uuid     {@link User}'s uuid.
     * @return {@link Page} of {@link Order}.
     */
    Page<Order> getAllByUserUuid(String uuid, Pageable pageable);

    /**
     * Returns a page with orders for the specified user and status matching one of
     * the specified statuses.
     *
     * @param pageable {@link Pageable} page to return.
     * @param uuid     {@link User}'s uuid.
     * @param statuses list of {@link OrderStatus} to match.
     * @return {@link Page} of {@link Order}.
     */
    Page<Order> getAllByUserUuidAndOrderStatus(String uuid, List<OrderStatus> statuses, Pageable pageable);
}
