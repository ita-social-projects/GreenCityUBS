package greencity.service.ubs.calculator;

import greencity.constant.AppConstant;
import greencity.dto.bag.BagInfoDto;
import greencity.dto.order.OrderInfoDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.dto.payment.PaymentWithStatusDto;
import greencity.dto.user.UserPointDto;
import greencity.enums.PaymentStatus;
import greencity.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {
    private final BagCalculatorService bagCalculatorService;
    private final PointCalculatorService pointCalculatorService;
    private final CertificateCalculatorService certificateCalculatorService;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public long calculateSumToPay(OrderWayForPayClientDto dto, OrderInfoDto orderInfo, UserPointDto userPoints) {
        long sumToPayInCoins = bagCalculatorService
            .getBagsSumToPayInCoins(orderInfo);

        sumToPayInCoins = certificateCalculatorService
            .getCertificateSumToPayInCoins(orderInfo.getId(), sumToPayInCoins);

        sumToPayInCoins = pointCalculatorService
            .getPointSumToPayInCoins(dto, userPoints, sumToPayInCoins);
        // Apply client certificates *after* points,
        // because points must always reduce sum before certificates.
        sumToPayInCoins = certificateCalculatorService
            .applyCertificatesForClientOrder(dto, orderInfo.getId(), sumToPayInCoins);

        List<PaymentWithStatusDto> payments = paymentRepository.findAllByOrderId(orderInfo.getId()).stream()
            .map(e -> modelMapper.map(e, PaymentWithStatusDto.class))
            .toList();
        return sumToPayInCoins - countPaidAmount(payments);
    }

    @Override
    public long countPaidAmount(List<PaymentWithStatusDto> payments) {
        return payments.stream()
            .filter(payment -> PaymentStatus.PAID.equals(payment.getPaymentStatus()))
            .map(PaymentWithStatusDto::getAmount)
            .mapToLong(Double::longValue)
            .reduce(0L, Long::sum);
    }

    @Override
    public long calculateOrderSumWithoutDiscounts(List<BagInfoDto> getOrderBagsAndQuantity) {
        return getOrderBagsAndQuantity.stream()
            .map(orderBag -> orderBag.getPrice() * orderBag.getAmount())
            .mapToLong(e -> (long) (e * AppConstant.CURRENCY_CONVERSION_RATE))
            .reduce(0L, Long::sum);
    }
}
