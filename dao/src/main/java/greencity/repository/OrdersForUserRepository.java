package greencity.repository;

import greencity.enums.OrderStatus;
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
     * Returns a page with orders for the specified user.
     *
     * @param pageable {@link Pageable} page to return.
     * @param uuid     {@link User}'s uuid.
     * @return {@link Page} of {@link Order}.
     */
    @Query(value = "SELECT o FROM Order AS o WHERE o.user = "
        + "(SELECT u FROM User AS u WHERE u.uuid = :uuid) "
        + "ORDER BY o.orderDate DESC")
    Page<Order> getAllByUserUuid(Pageable pageable, String uuid);

    /**
     * Returns a page with orders for the specified user and status matching one of
     * the specified statuses.
     *
     * @param pageable {@link Pageable} page to return.
     * @param uuid     {@link User}'s uuid.
     * @param statuses list of {@link OrderStatus} to match.
     * @return {@link Page} of {@link Order}.
     */

    @Query(value = "SELECT o FROM Order AS o WHERE o.user = "
        + "(SELECT u FROM User AS u WHERE u.uuid = :uuid) "
        + "AND o.orderStatus IN (:statuses) "
        + "ORDER BY o.orderDate DESC")
    Page<Order> getAllByUserUuidAndOrderStatusIn(Pageable pageable, String uuid, List<OrderStatus> statuses);

    /**
     * Returns an order for the specified user.
     *
     * @param uuid {@link User}'s uuid.
     * @return {@link Order}.
     */
    Order getAllByUserUuidAndId(String uuid, Long id);
}
