package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentSystemTest {
    @Test
    void testEnumValues() {
        assertNotNull(PaymentSystem.MONOBANK);
        assertNotNull(PaymentSystem.WAY_FOR_PAY);
    }

    @Test
    void testEnumByName() {
        assertEquals(PaymentSystem.MONOBANK, PaymentSystem.valueOf("MONOBANK"));
        assertEquals(PaymentSystem.WAY_FOR_PAY, PaymentSystem.valueOf("WAY_FOR_PAY"));
    }

    @Test
    void testEnumLength() {
        assertEquals(2, PaymentSystem.values().length);
    }

    @Test
    void testEnumValuesArray() {
        PaymentSystem[] expected = {PaymentSystem.MONOBANK, PaymentSystem.WAY_FOR_PAY};
        assertArrayEquals(expected, PaymentSystem.values());
    }
}
