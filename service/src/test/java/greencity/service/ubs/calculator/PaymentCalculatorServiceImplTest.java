package greencity.service.ubs.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.enums.PaymentStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentCalculatorServiceImplTest {
    @Mock
    private BagCalculatorService bagCalculatorService;
    @Mock
    private PointCalculatorService pointCalculatorService;
    @Mock
    private CertificateCalculatorService certificateCalculatorService;

    @InjectMocks
    private PaymentCalculatorServiceImpl paymentCalculatorService;

    @Test
    void calculateSumToPay_ShouldReturnCorrectValue() {
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        Order order = new Order();
        User user = new User();

        long bagsSum = 100L;
        long afterCertificates = 80L;
        long afterPoints = 60L;
        long afterClientCertificates = 50L;

        when(bagCalculatorService.getBagsSumToPayInCoins(order)).thenReturn(bagsSum);
        when(certificateCalculatorService.getCertificateSumToPayInCoins(order, bagsSum)).thenReturn(afterCertificates);
        when(pointCalculatorService.getPointSumToPayInCoins(dto, user, afterCertificates)).thenReturn(afterPoints);
        when(certificateCalculatorService.applyCertificatesForClientOrder(dto, order, afterPoints))
            .thenReturn(afterClientCertificates);

        Payment paidPayment = new Payment();
        paidPayment.setPaymentStatus(PaymentStatus.PAID);
        paidPayment.setAmount(20L);
        order.setPayment(List.of(paidPayment));

        long result = paymentCalculatorService.calculateSumToPay(dto, order, user);

        assertEquals(30L, result);
        verify(bagCalculatorService).getBagsSumToPayInCoins(order);
        verify(certificateCalculatorService).getCertificateSumToPayInCoins(order, bagsSum);
        verify(pointCalculatorService).getPointSumToPayInCoins(dto, user, afterCertificates);
        verify(certificateCalculatorService).applyCertificatesForClientOrder(dto, order, afterPoints);
    }

    @Test
    void countPaidAmount_ShouldReturnSumOfPaidPayments() {
        Payment p1 = new Payment();
        p1.setPaymentStatus(PaymentStatus.PAID);
        p1.setAmount(30L);

        Payment p2 = new Payment();
        p2.setPaymentStatus(PaymentStatus.UNPAID);
        p2.setAmount(40L);

        Payment p3 = new Payment();
        p3.setPaymentStatus(PaymentStatus.PAID);
        p3.setAmount(50L);

        List<Payment> payments = List.of(p1, p2, p3);

        Long result = paymentCalculatorService.countPaidAmount(payments);

        assertEquals(80L, result);
    }

    @Test
    void calculateOrderSumWithoutDiscounts_ShouldReturnCorrectValue() {
        OrderBag bag1 = new OrderBag();
        bag1.setPrice(10L);
        bag1.setAmount(2);

        OrderBag bag2 = new OrderBag();
        bag2.setPrice(5L);
        bag2.setAmount(3);

        List<OrderBag> bags = List.of(bag1, bag2);

        long result = paymentCalculatorService.calculateOrderSumWithoutDiscounts(bags);

        assertEquals(35L, result);
    }
}