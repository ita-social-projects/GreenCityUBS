package greencity.service.ubs.payment;

import greencity.dto.order.PaymentSystemResponse;
import greencity.entity.order.Order;
import greencity.enums.PaymentSystem;

public interface PaymentStrategy {
    PaymentSystem getPaymentSystem();

    PaymentSystemResponse processPayment(Order order, long sumToPayInCoins);
}
