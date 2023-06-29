package greencity.enums;

import org.junit.jupiter.api.Test;
import static greencity.enums.OrderStatus.getConvertedEnumFromLongToEnum;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void getNumValueTest() {
        assertEquals(1, OrderStatus.FORMED.getNumValue());
        assertEquals(2, OrderStatus.ADJUSTMENT.getNumValue());
        assertEquals(3, OrderStatus.BROUGHT_IT_HIMSELF.getNumValue());
        assertEquals(4, OrderStatus.CONFIRMED.getNumValue());
        assertEquals(5, OrderStatus.ON_THE_ROUTE.getNumValue());
        assertEquals(6, OrderStatus.DONE.getNumValue());
        assertEquals(7, OrderStatus.NOT_TAKEN_OUT.getNumValue());
        assertEquals(8, OrderStatus.CANCELED.getNumValue());
    }

    @Test
    void getConvertedEnumFromLongToEnumTest() {
        assertEquals(OrderStatus.ON_THE_ROUTE.toString(), getConvertedEnumFromLongToEnum(5L));
        assertEquals("", getConvertedEnumFromLongToEnum(-22L));
    }

    @Test
    void checkPossibleStatusTest() {
        assertTrue(OrderStatus.FORMED.checkPossibleStatus("ADJUSTMENT"));
        assertFalse(OrderStatus.ADJUSTMENT.checkPossibleStatus("ON_THE_ROUTE"));
    }
}
