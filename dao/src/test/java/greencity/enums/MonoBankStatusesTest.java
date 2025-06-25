package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonoBankStatusesTest {
    @Test
    void testEnumValues() {
        assertEquals(1, MonoBankStatuses.CREATED.getValue());
        assertEquals("created", MonoBankStatuses.CREATED.getName());

        assertEquals(2, MonoBankStatuses.PROCESSING.getValue());
        assertEquals("processing", MonoBankStatuses.PROCESSING.getName());

        assertEquals(3, MonoBankStatuses.HOLD.getValue());
        assertEquals("hold", MonoBankStatuses.HOLD.getName());

        assertEquals(4, MonoBankStatuses.SUCCESS.getValue());
        assertEquals("success", MonoBankStatuses.SUCCESS.getName());

        assertEquals(5, MonoBankStatuses.FAILURE.getValue());
        assertEquals("failure", MonoBankStatuses.FAILURE.getName());

        assertEquals(6, MonoBankStatuses.REVERSED.getValue());
        assertEquals("reversed", MonoBankStatuses.REVERSED.getName());

        assertEquals(7, MonoBankStatuses.EXPIRED.getValue());
        assertEquals("expired", MonoBankStatuses.EXPIRED.getName());
    }

    @Test
    void testEnumByName() {
        assertEquals(MonoBankStatuses.CREATED, MonoBankStatuses.valueOf("CREATED"));
        assertEquals(MonoBankStatuses.PROCESSING, MonoBankStatuses.valueOf("PROCESSING"));
        assertEquals(MonoBankStatuses.HOLD, MonoBankStatuses.valueOf("HOLD"));
        assertEquals(MonoBankStatuses.SUCCESS, MonoBankStatuses.valueOf("SUCCESS"));
        assertEquals(MonoBankStatuses.FAILURE, MonoBankStatuses.valueOf("FAILURE"));
        assertEquals(MonoBankStatuses.REVERSED, MonoBankStatuses.valueOf("REVERSED"));
        assertEquals(MonoBankStatuses.EXPIRED, MonoBankStatuses.valueOf("EXPIRED"));
    }
}
