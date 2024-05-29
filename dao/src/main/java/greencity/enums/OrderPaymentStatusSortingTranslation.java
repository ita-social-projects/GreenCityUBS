package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum OrderPaymentStatusSortingTranslation {
    UNPAID(1),
    PAYMENT_REFUNDED(2),
    PAID(3),
    HALF_PAID(4),
    OTHER(5);

    private int sortingOrder;

    public static Map<OrderPaymentStatusSortingTranslation, Integer> orderPaymentStatusSortingMapByAsc() {
        Map<OrderPaymentStatusSortingTranslation, Integer> orderPaymentStatusSortingMap =
            new HashMap<>();
        for (OrderPaymentStatusSortingTranslation translation : OrderPaymentStatusSortingTranslation.values()) {
            orderPaymentStatusSortingMap.put(translation, translation.getSortingOrder());
        }
        return orderPaymentStatusSortingMap;
    }

    public static Map<OrderPaymentStatusSortingTranslation, Integer> orderStatusSortingTranslationIntegerMapByDesc() {
        Map<OrderPaymentStatusSortingTranslation, Integer> orderStatusSortingTranslationIntegerMap = new HashMap<>();
        int maxValue = OrderStatusSortingTranslation.values().length;
        for (OrderPaymentStatusSortingTranslation translation : OrderPaymentStatusSortingTranslation.values()) {
            orderStatusSortingTranslationIntegerMap.put(translation, maxValue - translation.getSortingOrder() + 1);
        }
        return orderStatusSortingTranslationIntegerMap;
    }
}
