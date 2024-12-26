package greencity.enums;

import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPaymentStatusSortingTranslationTest {
    @Test
    void testGetSortOrder() {
        assertEquals(1, OrderPaymentStatusSortingTranslation.UNPAID.getSortOrder());
        assertEquals(2, OrderPaymentStatusSortingTranslation.PAYMENT_REFUNDED.getSortOrder());
        assertEquals(3, OrderPaymentStatusSortingTranslation.PAID.getSortOrder());
        assertEquals(4, OrderPaymentStatusSortingTranslation.HALF_PAID.getSortOrder());
        assertEquals(5, OrderPaymentStatusSortingTranslation.OTHER.getSortOrder());
    }

    @Test
    void testOrderSetSortedByAsc() {
        Set<OrderPaymentStatusSortingTranslation> expectedAscSet =
            EnumSet.allOf(OrderPaymentStatusSortingTranslation.class);
        Set<OrderPaymentStatusSortingTranslation> actualAscSet =
            OrderPaymentStatusSortingTranslation.OTHER.getSortedTranslations();
        assertEquals(expectedAscSet, actualAscSet);
    }
}
