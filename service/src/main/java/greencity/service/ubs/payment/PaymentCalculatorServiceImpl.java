package greencity.service.ubs.payment;

import greencity.constant.AppConstant;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.enums.PaymentStatus;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.calculator.BagCalculatorService;
import greencity.util.PointsUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {
    private final ModelMapper modelMapper;
    private final CertificateService certificateService;
    private final BagCalculatorService bagCalculatorService;
    private final PointsUtils pointsUtils;

    //TODO clean up this method to make it more readable
    @Override
    public long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser) {
        long sumToPayInCoins = bagCalculatorService.getBagsSumToPayInCoins(order);

        //TODO think how to move this to certificateCalculate
        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .toList();

        sumToPayInCoins = sumToPayInCoins - (long) AppConstant.CURRENCY_CONVERSION_RATE * (order.getPointsToUse()
            + certificateService.countCertificatesBonuses(certificateDtos));

        //TODO think how to move this to Point
        pointsUtils.checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayInCoins, dto.getPointsToUse());
        //TODO why this is goes after point calculating think how to move this
        sumToPayInCoins = certificateService
            .formCertificatesToBeSavedAndCalculateOrderSumClient(dto, order, sumToPayInCoins);

        return sumToPayInCoins - countPaidAmount(order.getPayment());
    }

    //TODO move to Point
    @Override
    public long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse) {
        if (sumToPayInCoins >= pointsToUse * (long) AppConstant.CURRENCY_CONVERSION_RATE) {
            sumToPayInCoins -= pointsToUse * (long) AppConstant.CURRENCY_CONVERSION_RATE;
        }
        return sumToPayInCoins;
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
