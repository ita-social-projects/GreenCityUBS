package greencity.enums;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
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
    public void testOrderListSortedByAsc() {
        List<OrderStatusSortingTranslation> expectedAscList = Arrays.asList(
            OrderStatusSortingTranslation.DONE,
            OrderStatusSortingTranslation.ON_THE_ROUTE,
            OrderStatusSortingTranslation.NOT_TAKEN_OUT,
            OrderStatusSortingTranslation.CONFIRMED,
            OrderStatusSortingTranslation.BROUGHT_IT_HIMSELF,
            OrderStatusSortingTranslation.CANCELED,
            OrderStatusSortingTranslation.FORMED,
            OrderStatusSortingTranslation.ADJUSTMENT,
            OrderStatusSortingTranslation.OTHER);

        List<OrderStatusSortingTranslation> actualAscList = OrderStatusSortingTranslation.getOrderListSortedByAsc();

        assertEquals(expectedAscList, actualAscList);
    }
}
