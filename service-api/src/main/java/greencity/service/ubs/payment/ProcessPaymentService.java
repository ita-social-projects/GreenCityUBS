package greencity.service.ubs.payment;

import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;
import greencity.entity.user.User;

//TODO add test
public interface ProcessPaymentService {
    /**
     * Methods creates and adjusts new order and generates payment link for the
     * order.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @return {@link PaymentSystemResponse} which contains data to pay order out.
     * @author Oleksandr Ilnytskyi
     */
    PaymentSystemResponse processNewOrder(OrderResponseDto dto, String uuid);

    /**
     * Methods adjusts existing order and generates payment link for the order if
     * order is unpaid.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current {@link User}'s uuid;
     * @return {@link PaymentSystemResponse} which contains data to pay order out.
     * @author Oleksandr Ilnytskyi
     */
    PaymentSystemResponse processExistingOrder(OrderResponseDto dto, String uuid, Long orderId);

    /**
     * Method to generate payment link.
     *
     * @param userUuid current {@link User} uuid.
     * @param dto      order information.
     * @return {@link PaymentSystemResponse} payment link and order id.
     */
    PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto);

    /**
     * Forms a payment link for redirecting the user to WayForPay checkout.
     * - Increments the order’s counter for payment attempts. <br>
     * - Creates a {@link PaymentWayForPayRequestDto} request with encoded order reference. <br>
     * - Sends the request to WayForPay checkout client. <br>
     * - Extracts and returns the redirect URL from the WayForPay response.
     *
     * @param order           the {@link Order} for which the payment link is generated
     * @param sumToPayInCoins the amount to be paid in coins
     * @return the checkout URL where the client should be redirected to complete payment
     */
    String formedLink(Order order, long sumToPayInCoins);
}
