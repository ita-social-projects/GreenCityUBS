package greencity.service.ubs.payment;

import greencity.dto.bag.BagForUserDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import java.util.List;
import java.util.Set;

public interface PaymentCalculatorService {
    long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser);

    //TODO add docs
    List<BagForUserDto> bagForUserDtosBuilder(Order order);

    Long getSumToPay(List<BagForUserDto> bagForUserDtos);

    Long convertBillsIntoCoins(Double bills);

    long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse);

    Long countPaidAmount(List<Payment> payments);

    Double convertCoinsIntoBills(Long coins);

    long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity);

    long formCertificatesToBeSavedAndCalculateOrderSum(OrderResponseDto dto, Set<Certificate> orderCertificates,
                                                       Order order, long sumToPayInCoins);
}
