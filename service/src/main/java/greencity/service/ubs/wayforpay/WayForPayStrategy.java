package greencity.service.ubs.wayforpay;

import greencity.dto.order.PaymentSystemResponse;
import greencity.entity.order.Order;
import greencity.enums.PaymentSystem;
import greencity.service.ubs.payment.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WayForPayStrategy implements PaymentStrategy {
    private final WayForPayService wayForPayService;

    @Override
    public PaymentSystem getPaymentSystem() {
        return PaymentSystem.WAY_FOR_PAY;
    }

    @Override
    @Transactional
    public PaymentSystemResponse processPayment(Order order, long sumToPayInCoins) {
        return wayForPayService.processWayForPay(order, sumToPayInCoins);
    }
}
