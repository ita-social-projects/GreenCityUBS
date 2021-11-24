package greencity.repository;

import greencity.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
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
}
