package greencity.enums;

import greencity.entity.order.Bag;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BagStatusTest {
    private static Bag getBag(BagStatus status) {
        return Bag.builder()
            .status(status)
            .id(1)
            .capacity(120)
            .commission(50_00L)
            .price(120_00L)
            .fullPrice(170_00L)
            .createdAt(LocalDate.now())
            .limitIncluded(true)
            .build();
    }

    @Test
    void testGetStatus() {
        assertEquals(BagStatus.ACTIVE, getBag(BagStatus.ACTIVE).getStatus());
    }

    @Test
    void testSetStatus() {
        assertEquals(BagStatus.DELETED, getBag(BagStatus.DELETED).getStatus());
    }
}