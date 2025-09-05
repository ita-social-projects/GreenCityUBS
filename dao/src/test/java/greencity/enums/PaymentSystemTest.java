package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentSystemTest {
    @Test
    void testEnumValues() {
        assertNotNull(PaymentSystem.WAY_FOR_PAY);
    }

    @Test
    void testEnumByName() {
        assertEquals(PaymentSystem.WAY_FOR_PAY, PaymentSystem.valueOf("WAY_FOR_PAY"));
    }

    @Test
    void testEnumLength() {
        assertEquals(1, PaymentSystem.values().length);
    }

    @Test
    void testEnumValuesArray() {
        PaymentSystem[] expected = {PaymentSystem.WAY_FOR_PAY};
        assertArrayEquals(expected, PaymentSystem.values());
    }
}
