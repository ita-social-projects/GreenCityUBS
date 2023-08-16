package greencity.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class BagStatusTest {

    @Test
    void enumValuesExistenceTest() {
        assertNotNull(BagStatus.ACTIVE);
        assertNotNull(BagStatus.DELETED);
    }

    @Test
    void enumValuesUniquenessTest() {
        assertNotEquals(BagStatus.ACTIVE, BagStatus.DELETED);
    }

    @Test
    void enumValuesToStringTest() {
        assertEquals("ACTIVE", BagStatus.ACTIVE.toString());
        assertEquals("DELETED", BagStatus.DELETED.toString());
    }

    @Test
    void valueOfTest() {
        assertEquals(BagStatus.ACTIVE, BagStatus.valueOf("ACTIVE"));
        assertEquals(BagStatus.DELETED, BagStatus.valueOf("DELETED"));
    }

    @Test
    void enumOrdinalTest() {
        assertEquals(0, BagStatus.ACTIVE.ordinal());
        assertEquals(1, BagStatus.DELETED.ordinal());
    }

    @Test
    void enumValuesArrayTest() {
        BagStatus[] expectedArray = {BagStatus.ACTIVE, BagStatus.DELETED};
        assertArrayEquals(expectedArray, BagStatus.values());
    }
}
