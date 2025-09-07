package greencity.service.ubs.payment;

import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.entity.order.Payment;
import greencity.entity.user.User;
import java.util.List;

//TODO add docs
//TODO add tests
public interface PaymentCalculatorService {
    long calculateSumToPay(OrderWayForPayClientDto dto, Order order, User currentUser);

    long reduceOrderSumDueToUsedPoints(long sumToPayInCoins, int pointsToUse);

    Long countPaidAmount(List<Payment> payments);

    long calculateOrderSumWithoutDiscounts(List<OrderBag> getOrderBagsAndQuantity);
}
