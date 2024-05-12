package greencity.enums;

import java.util.HashMap;
import java.util.Map;

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

    OrderStatusSortingTranslation(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static Map<OrderStatusSortingTranslation, Integer> getOrderMapSortedByAsc() {
        Map<OrderStatusSortingTranslation, Integer> sortOrderMap = new HashMap<>();
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            sortOrderMap.put(status, status.getSortOrder());
        }
        return sortOrderMap;
    }

    public static Map<OrderStatusSortingTranslation, Integer> getOrderMapSortedByDesc() {
        Map<OrderStatusSortingTranslation, Integer> sortOrderMap = new HashMap<>();
        int maxOrder = OrderStatusSortingTranslation.values().length;
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            sortOrderMap.put(status, maxOrder - status.getSortOrder() + 1);
        }
        return sortOrderMap;
    }
}
