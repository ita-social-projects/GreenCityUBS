package greencity.enums;

import lombok.Getter;
import java.util.EnumMap;

@Getter
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

    public static EnumMap<OrderStatusSortingTranslation, Integer> getOrderMapSortedByAsc() {
        EnumMap<OrderStatusSortingTranslation, Integer> sortOrderMap =
            new EnumMap<>(OrderStatusSortingTranslation.class);
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            sortOrderMap.put(status, status.getSortOrder());
        }
        return sortOrderMap;
    }

    public static EnumMap<OrderStatusSortingTranslation, Integer> getOrderMapSortedByDesc() {
        EnumMap<OrderStatusSortingTranslation, Integer> sortOrderMap =
            new EnumMap<>(OrderStatusSortingTranslation.class);
        int maxOrder = OrderStatusSortingTranslation.values().length;
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            sortOrderMap.put(status, maxOrder - status.getSortOrder() + 1);
        }
        return sortOrderMap;
    }
}
