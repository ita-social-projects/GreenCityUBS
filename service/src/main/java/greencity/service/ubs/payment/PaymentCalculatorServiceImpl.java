package greencity.service.ubs.payment;

import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.enums.PaymentStatus;
import greencity.service.ubs.calculator.BagCalculatorService;
import greencity.service.ubs.calculator.CertificateCalculatorService;
import greencity.service.ubs.calculator.PointCalculatorService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {
    private final BagCalculatorService bagCalculatorService;
    private final PointCalculatorService pointCalculatorService;
    private final CertificateCalculatorService certificateCalculatorService;

    @Override
    @Transactional
    public long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser) {
        long sumToPayInCoins = bagCalculatorService
            .getBagsSumToPayInCoins(order);

        sumToPayInCoins = certificateCalculatorService
            .getCertificateSumToPayInCoins(order, sumToPayInCoins);

        sumToPayInCoins = pointCalculatorService
            .getPointSumToPayInCoins(dto, currentUser, sumToPayInCoins);
        // Apply client certificates *after* points,
        // because points must always reduce sum before certificates.
        sumToPayInCoins = certificateCalculatorService
            .applyCertificatesForClientOrder(dto, order, sumToPayInCoins);

        return sumToPayInCoins - countPaidAmount(order.getPayment());
    }

    @Override
    public Long countPaidAmount(List<Payment> payments) {
        return payments.stream()
            .filter(payment -> PaymentStatus.PAID.equals(payment.getPaymentStatus()))
            .map(Payment::getAmount)
            .reduce(0L, Long::sum);
    }

    @Override
    public long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity) {
        return getOrderBagsAndQuantity.stream()
            .map(orderBag -> orderBag.getPrice() * orderBag.getAmount())
            .reduce(0L, Long::sum);
    }
}
