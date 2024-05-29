package greencity.enums;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatusSortingTranslationTest {
    @Test
    void testGetSortOrder() {
        assertEquals(1, OrderStatusSortingTranslation.DONE.getSortOrder());
        assertEquals(2, OrderStatusSortingTranslation.ON_THE_ROUTE.getSortOrder());
        assertEquals(3, OrderStatusSortingTranslation.NOT_TAKEN_OUT.getSortOrder());
        assertEquals(4, OrderStatusSortingTranslation.CONFIRMED.getSortOrder());
        assertEquals(5, OrderStatusSortingTranslation.BROUGHT_IT_HIMSELF.getSortOrder());
        assertEquals(6, OrderStatusSortingTranslation.CANCELED.getSortOrder());
        assertEquals(7, OrderStatusSortingTranslation.FORMED.getSortOrder());
        assertEquals(8, OrderStatusSortingTranslation.ADJUSTMENT.getSortOrder());
        assertEquals(9, OrderStatusSortingTranslation.OTHER.getSortOrder());
    }

    @Test
    void testGetOrderMapSortedByAsc() {
        Map<OrderStatusSortingTranslation, Integer> sortedAsc = OrderStatusSortingTranslation.getOrderMapSortedByAsc();

        assertEquals(OrderStatusSortingTranslation.values().length, sortedAsc.size());

        int[] expectedOrderAsc = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int i = 0;
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            assertEquals(expectedOrderAsc[i++], sortedAsc.get(status));
        }
    }

    @Test
    void testGetOrderMapSortedByDesc() {
        Map<OrderStatusSortingTranslation, Integer> sortedDesc =
            OrderStatusSortingTranslation.getOrderMapSortedByDesc();

        assertEquals(OrderStatusSortingTranslation.values().length, sortedDesc.size());

        int[] expectedOrderDesc = {9, 8, 7, 6, 5, 4, 3, 2, 1};
        int i = 0;
        for (OrderStatusSortingTranslation status : OrderStatusSortingTranslation.values()) {
            assertEquals(expectedOrderDesc[i++], sortedDesc.get(status));
        }
    }
}
