package greencity.service.ubs.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.dto.user.UserPointDto;
import greencity.entity.order.Payment;
import greencity.enums.PaymentStatus;
import greencity.repository.PaymentRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class PaymentCalculatorServiceImplTest {
    @Mock
    private BagCalculatorService bagCalculatorService;
    @Mock
    private PointCalculatorService pointCalculatorService;
    @Mock
    private CertificateCalculatorService certificateCalculatorService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentCalculatorServiceImpl paymentCalculatorService;

    @Test
    void calculateSumToPay_ShouldReturnCorrectValue() {
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        OrderInfoDto order = ModelUtils.getOrderInfoDto();
        UserPointDto user = new UserPointDto();
        Payment paidPayment = new Payment();
        paidPayment.setPaymentStatus(PaymentStatus.PAID);
        paidPayment.setAmount(20L);

        long bagsSum = 10000L;
        long afterCertificates = 8000L;
        long afterPoints = 6000L;
        long afterClientCertificates = 5000L;

        when(bagCalculatorService.getBagsSumToPayInCoins(order)).thenReturn(bagsSum);
        when(certificateCalculatorService.getCertificateSumToPayInCoins(order.getId(), bagsSum))
            .thenReturn(afterCertificates);
        when(pointCalculatorService.getPointSumToPayInCoins(dto, user, afterCertificates)).thenReturn(afterPoints);
        when(certificateCalculatorService.applyCertificatesForClientOrder(dto, order.getId(), afterPoints))
            .thenReturn(afterClientCertificates);
        when(paymentRepository.findAllByOrderId(order.getId())).thenReturn(List.of(paidPayment));
        when(modelMapper.map(paidPayment, PaymentWithStatusDto.class))
            .thenReturn(PaymentWithStatusDto.builder()
                .id(paidPayment.getId())
                .paymentStatus(paidPayment.getPaymentStatus())
                .amount(paidPayment.getAmount().doubleValue())
                .build());

        long result = paymentCalculatorService.calculateSumToPay(dto, order, user);

        assertEquals(3000L, result);
        verify(bagCalculatorService).getBagsSumToPayInCoins(order);
        verify(certificateCalculatorService).getCertificateSumToPayInCoins(order.getId(), bagsSum);
        verify(pointCalculatorService).getPointSumToPayInCoins(dto, user, afterCertificates);
        verify(certificateCalculatorService).applyCertificatesForClientOrder(dto, order.getId(), afterPoints);
    }

    @Test
    void countPaidAmount_ShouldReturnSumOfPaidPayments() {
        PaymentWithStatusDto p1 = new PaymentWithStatusDto();
        p1.setPaymentStatus(PaymentStatus.PAID);
        p1.setAmount(30.0);// 3000

        PaymentWithStatusDto p2 = new PaymentWithStatusDto();
        p2.setPaymentStatus(PaymentStatus.UNPAID);
        p2.setAmount(40.0);// 4000

        PaymentWithStatusDto p3 = new PaymentWithStatusDto();
        p3.setPaymentStatus(PaymentStatus.PAID);
        p3.setAmount(50.0);// 5000

        List<PaymentWithStatusDto> payments = List.of(p1, p2, p3);

        Long result = paymentCalculatorService.countPaidAmount(payments);

        assertEquals(8000L, result);
    }

    @Test
    void calculateOrderSumWithoutDiscounts_ShouldReturnCorrectValue() {
        BagInfoDto bag1 = new BagInfoDto();
        bag1.setPrice(10.0);
        bag1.setAmount(2);

        BagInfoDto bag2 = new BagInfoDto();
        bag2.setPrice(5.0);
        bag2.setAmount(3);

        List<BagInfoDto> bags = List.of(bag1, bag2);

        long result = paymentCalculatorService.calculateOrderSumWithoutDiscounts(bags);

        assertEquals(3500L, result);
    }
}