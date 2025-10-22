package greencity.service.ubs.payment;

import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import java.util.List;
import java.util.Set;

public interface ProcessPaymentService {
    /**
     * Methods creates and adjusts new order and generates payment link for the
     * order.
     *
     * @param dto  {@link OrderResponseDto} user entered data;
     * @param uuid current user's uuid;
     * @return {@link PaymentSystemResponse} which contains data to pay order out.
     * @author Oleksandr Ilnytskyi
     */
    PaymentSystemResponse processNewOrder(OrderResponseDto dto, String uuid);

    /**
     * Methods adjusts existing order and generates payment link for the order if
     * order is unpaid.
     *
     * @param dto     {@link OrderResponseDto} user entered data;
     * @param uuid    current user's uuid;
     * @param orderId {@link Long} id of the order to adjust;
     * @return {@link PaymentSystemResponse} which contains data to pay order out.
     * @author Oleksandr Ilnytskyi
     */
    PaymentSystemResponse processExistingOrder(OrderResponseDto dto, String uuid, Long orderId);

    /**
     * Method to generate payment link.
     *
     * @param userUuid current user uuid.
     * @param dto      order information.
     * @return {@link PaymentSystemResponse} payment link and order id.
     */
    PaymentSystemResponse processOrder(String userUuid, OrderWayForPayClientDto dto);

    /**
     * Forms a payment link for redirecting the user to WayForPay checkout. -
     * Increments the order’s counter for payment attempts. - Creates a
     * {@link PaymentWayForPayRequestDto} request with encoded order reference. -
     * Sends the request to WayForPay checkout client. - Extracts and returns the
     * redirect URL from the WayForPay response. - Schedules a payment expiry job
     * for the generated link.
     *
     * @param orderId           the {order id for which the payment link is
     *                        generated
     * @param sumToPayInCoins the amount to be paid in coins
     * @return the checkout URL where the client should be redirected to complete
     *         payment
     */
    String formedLink(Long orderId, long sumToPayInCoins);

    /**
     * Forms a payment link for redirecting the user to WayForPay checkout, using
     * additional client data. - Increments the order’s counter for payment
     * attempts. - Creates a {@link PaymentWayForPayRequestDto} request with encoded
     * order reference. - Sends the request to WayForPay checkout client and
     * extracts the redirect URL. - Schedules a payment expiry job for the generated
     * link.
     *
     * @param orderId           the order id for which the payment link is
     *                        generated
     * @param sumToPayInCoins the amount to be paid in coins
     * @param dto             additional payment data such as used points and
     *                        certificates
     * @return the checkout URL where the client should be redirected to complete
     *         payment
     */
    String formedLink(Long orderId, long sumToPayInCoins, OrderWayForPayClientDto dto);

    /**
     * Method removes payment link data and returns specified certificates/points to
     * user with proper change of points reason.
     *
     * @param orderId          {@link Long} id of order to modify;
     * @param pointsToUse      {@link Integer} amount of points to be returned;
     * @param certificateCodes {@link List} of certificate {@link String} codes to
     *                         be refunded;
     * @author Oleksandr Ilnytskyi
     */
    void expirePaymentAttempt(Long orderId, int pointsToUse, Set<String> certificateCodes);

    /**
     * This method cancels invoice, sets payment link empty and fires payment expiry
     * job after, which returns points/certificates from that attempt to user
     * account.
     *
     * @param uuid    current user's uuid.
     * @param orderId id of the order that belongs to user.
     * @author Oleksandr Ilnytskyi
     */
    void cancelPaymentAttempt(String uuid, Long orderId);
}
