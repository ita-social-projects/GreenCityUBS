package greencity.service.ubs.payment;

import greencity.constant.AppConstant;
import greencity.dto.bag.BagForUserDto;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import greencity.enums.PaymentStatus;
import greencity.service.ubs.CertificateService;
import greencity.service.ubs.OrderBagService;
import greencity.util.PointsUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {
    private final ModelMapper modelMapper;
    private final OrderBagService orderBagService;
    private final CertificateService certificateService;
    private final PointsUtils pointsUtils;

    @Override
    public long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser) {
        List<BagForUserDto> bagForUserDtos = bagForUserDtosBuilder(order);
        long sumToPayInCoins = getSumToPay(bagForUserDtos);

        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .toList();

        sumToPayInCoins = sumToPayInCoins - 100L * (order.getPointsToUse()
            + certificateService.countCertificatesBonuses(certificateDtos));

        pointsUtils.checkIfUserHaveEnoughPoints(currentUser.getCurrentPoints(), dto.getPointsToUse());
        sumToPayInCoins = reduceOrderSumDueToUsedPoints(sumToPayInCoins, dto.getPointsToUse());
        sumToPayInCoins = certificateService
            .formCertificatesToBeSavedAndCalculateOrderSumClient(dto, order, sumToPayInCoins);

        return sumToPayInCoins - countPaidAmount(order.getPayment());
    }

    @Override
    public Long getSumToPay(List<BagForUserDto> bagForUserDtos) {
        return bagForUserDtos.stream()
            .map(b -> convertBillsIntoCoins(b.getTotalPrice()))
            .reduce(0L, Long::sum);
    }

    @Override
    public List<BagForUserDto> bagForUserDtosBuilder(Order order) {
        List<OrderBag> bagsAmountInOrder = order.getOrderBags();
        Map<Integer, Integer> actualBagsAmount = orderBagService.getActualBagsAmountForOrder(bagsAmountInOrder);
        return bagsAmountInOrder.stream()
            .map(orderBag -> buildBagForUserDto(orderBag, actualBagsAmount.get(orderBag.getBag().getId())))
            .toList();
    }

    //TODO think to remove from interface
    @Override
    public Long convertBillsIntoCoins(Double bills) {
        return BigDecimal.valueOf(bills)
            .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .longValue();
    }

    @Override
    public long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse) {
        if (sumToPayInCoins >= pointsToUse * 100L) {
            sumToPayInCoins -= pointsToUse * 100L;
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
    public Double convertCoinsIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
            .doubleValue();
    }

    @Override
    public long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity) {
        return getOrderBagsAndQuantity.stream()
            .map(orderBag -> orderBag.getPrice() * orderBag.getAmount())
            .reduce(0L, Long::sum);
    }

    @Override
    public long formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
                                                              Order order, long sumToPayInCoins) {
        return 0;
    }

    private BagForUserDto buildBagForUserDto(OrderBag orderBag, int count) {
        BagForUserDto bagDto = modelMapper.map(orderBag, BagForUserDto.class);
        bagDto.setCount(count);
        bagDto.setTotalPrice(convertCoinsIntoBills(count * orderBag.getPrice()));
        return bagDto;
    }
}
