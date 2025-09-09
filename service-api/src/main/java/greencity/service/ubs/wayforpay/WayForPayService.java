package greencity.service.ubs.wayforpay;

import greencity.dto.order.PaymentSystemResponse;
import greencity.dto.payment.PaymentWayForPayRequestDto;
import greencity.entity.order.Order;

//TODO add docs
//TODO add test
public interface WayForPayService {
    PaymentSystemResponse processWayForPay(Order order, long sumToPayInCoins);

    PaymentWayForPayRequestDto formPaymentRequestForWayForPay(Long orderId, long sumToPayInCoins);

    PaymentSystemResponse getPaymentRequestDto(Order order, String link);

    String getLinkFromWayForPayCheckoutResponse(String wayForPayResponse);
}
