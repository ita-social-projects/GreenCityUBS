package greencity.service.ubs.order;

import greencity.dto.order.OrderResponseDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;

//TODO add test

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
}
