package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum OrderStatusSortingTranslation {
    DONE(1),
    ON_THE_ROUTE(2),
    NOT_TAKEN_OUT(3),
    CONFIRMED(4),
    BROUGHT_IT_HIMSELF(5),
    CANCELED(6),
    FORMED(7),
    ADJUSTMENT(8),
    OTHER(9);

    private final int sortOrder;

    private static final Set<OrderStatusSortingTranslation> ASC_ORDER_SET =
        Collections.unmodifiableSet(EnumSet.allOf(OrderStatusSortingTranslation.class));

    /**
     * This method return Set collection of OrderStatus translation in ascending
     * order.
     *
     * @return Set of {@link OrderStatusSortingTranslation}
     */
    public static Set<OrderStatusSortingTranslation> getOrderSetSortedByAsc() {
        return ASC_ORDER_SET;
    }
}