package greencity.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationTemplateSortTypeTest {

    @Test
    void enumValuesExistenceTest() {
        assertNotNull(NotificationTemplateSortType.TITLE);
        assertNotNull(NotificationTemplateSortType.TRIGGER);
        assertNotNull(NotificationTemplateSortType.STATUS_ACTIVE);
        assertNotNull(NotificationTemplateSortType.STATUS_INACTIVE);
    }

    @Test
    void enumValuesToStringTest() {
        assertEquals("TITLE", NotificationTemplateSortType.TITLE.toString());
        assertEquals("TRIGGER", NotificationTemplateSortType.TRIGGER.toString());
        assertEquals("STATUS_ACTIVE", NotificationTemplateSortType.STATUS_ACTIVE.toString());
        assertEquals("STATUS_INACTIVE", NotificationTemplateSortType.STATUS_INACTIVE.toString());
    }
}
