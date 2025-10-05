package greencity.service.ubs.order;

import greencity.dto.order.OrderCancellationReasonDto;
import greencity.dto.order.OrderPaymentDetailDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrdersDataForUserDto;
import greencity.dto.pageble.PageableDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.enums.OrderStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Service for creating and updating {@link Order} entities,
 * including applying points and forming full order details.
 */
public interface OrderService {
    /**
     * Forms and saves an {@link Order} based on provided request data.
     * - Validates tariffs for the selected bags and location.
     * - Calculates the total sum including discounts, points, and certificates.
     * - Updates the order with related entities (bags, certificates, tariffs, UBS user, etc.).
     * - Persists the order and its associated payment in the database.
     *
     * @param dto         the {@link OrderResponseDto} containing order request details
     * @param order       the existing or new {@link Order} to populate
     * @param currentUser the current authenticated {@link User}
     * @param userData    the {@link UBSuser} containing personal and address info
     * @return the saved {@link Order} entity with calculated payment details
     * @throws NotFoundException if tariffs, bags, or related entities cannot be found
     * @throws BadRequestException if points usage is invalid
     */
    Order formAndSaveOrderRequest(OrderResponseDto dto, Order order, User currentUser, UBSuser userData);

    /**
     * Transfers user points to the given {@link Order}.
     * - Validates that the user has enough points. <br>
     * - Ensures points do not exceed the remaining order amount. <br>
     * - Updates both the user’s balance and the order’s points usage. <br>
     * - Saves changes and records a points transaction event.
     *
     * @param order       the {@link Order} to apply points to
     * @param pointsToUse the number of points the user wants to use
     * @throws BadRequestException if points exceed the maximum allowed or user balance
     */
    void transferUserPointsToOrder(Order order, Integer pointsToUse);

    /**
     * Method that returns info about all orders for specified userID.
     *
     * @param uuid current {@link User}'s uuid;
     * @author Oleksandr Khomiakov
     */
    PageableDto<OrdersDataForUserDto> getOrdersForUser(String uuid, Pageable page, List<OrderStatus> statuses);

    /**
     * Method that returns info about order for specified userID.
     *
     * @param uuid current {@link User}'s uuid;
     * @author Oleg Postolovskyi
     */
    OrdersDataForUserDto getOrderForUser(String uuid, Long id);

    /**
     * Method returns information about order payment by orderId.
     *
     * @param orderId {@link Long}
     * @return {@link OrderPaymentDetailDto} dto that contain information about
     *         order payment.
     * @author Mykola Danylko
     */
    OrderPaymentDetailDto getOrderPaymentDetail(Long orderId);

    /**
     * Method returns cancellation reason and comment.
     *
     * @param orderId {@link Long};
     * @param uuid    current {@link User}'s uuid;
     * @return {@link OrderCancellationReasonDto} dto that contains cancellation
     *         reason and comment;
     * @author Oleksandr Khomiakov
     */
    OrderCancellationReasonDto getOrderCancellationReason(Long orderId, String uuid);

    /**
     * Method for delete user order.
     *
     * @param id - current order id.
     * @author Max Boyarchuk
     */
    void deleteOrder(String uuid, Long id);

    /**
     * Aggregates order details into a user-facing DTO.
     *
     * <p>Calculates total price, discounts from certificates and points,
     * paid amount, refunds, and localized statuses. The result is
     * returned as {@link OrdersDataForUserDto} for displaying order info
     * to the user.</p>
     *
     * @param order the {@link Order} entity with all related data
     * @return aggregated {@link OrdersDataForUserDto}
     */
    public OrdersDataForUserDto getOrdersData(Order order);
}
