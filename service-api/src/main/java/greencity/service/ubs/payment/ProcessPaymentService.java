package greencity.service.ubs.payment;

import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.entity.order.Order;
import greencity.entity.user.User;

//TODO add docs
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

    String formedLink(Order order, long sumToPayInCoins);
}
