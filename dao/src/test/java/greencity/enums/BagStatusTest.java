package greencity.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class BagStatusTest {

    @Test
    public void enumValuesExistenceTest() {
        assertNotNull(BagStatus.ACTIVE);
        assertNotNull(BagStatus.DELETED);
    }

    @Test
    public void enumValuesUniquenessTest() {
        assertNotEquals(BagStatus.ACTIVE, BagStatus.DELETED);
    }

    @Test
    public void enumValuesToStringTest() {
        assertEquals("ACTIVE", BagStatus.ACTIVE.toString());
        assertEquals("DELETED", BagStatus.DELETED.toString());
    }

    @Test
    public void valueOfTest() {
        assertEquals(BagStatus.ACTIVE, BagStatus.valueOf("ACTIVE"));
        assertEquals(BagStatus.DELETED, BagStatus.valueOf("DELETED"));
    }

    @Test
    public void enumOrdinalTest() {
        assertEquals(0, BagStatus.ACTIVE.ordinal());
        assertEquals(1, BagStatus.DELETED.ordinal());
    }

    @Test
    public void enumValuesArrayTest() {
        BagStatus[] expectedArray = {BagStatus.ACTIVE, BagStatus.DELETED};
        assertArrayEquals(expectedArray, BagStatus.values());
    }
}
