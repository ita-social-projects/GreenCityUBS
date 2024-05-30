package greencity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final List<OrderStatusSortingTranslation> ASC_ORDER_LIST = new ArrayList<>();
    private static final List<OrderStatusSortingTranslation> DESC_ORDER_LIST = new ArrayList<>();

    static {
        Collections.addAll(ASC_ORDER_LIST, OrderStatusSortingTranslation.values());
        Collections.addAll(DESC_ORDER_LIST, OrderStatusSortingTranslation.values());
        Collections.reverse(DESC_ORDER_LIST);
    }

    public static List<OrderStatusSortingTranslation> getOrderListSortedByAsc() {
        return ASC_ORDER_LIST;
    }
}
