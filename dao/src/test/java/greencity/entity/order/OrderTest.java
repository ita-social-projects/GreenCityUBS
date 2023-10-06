package greencity.entity.order;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {
    @Test
    void updateWithNewOrderBagsTest() {
        Order order = new Order();
        OrderBag oldBag = OrderBag.builder().id(4L).build();
        order.addOrderedBag(oldBag);
        List<OrderBag> previous = order.getOrderBags();
        List<OrderBag> bags = List.of(
            OrderBag.builder().id(1L).build(),
            OrderBag.builder().id(2L).build(),
            OrderBag.builder().id(3L).build());

        order.updateWithNewOrderBags(bags);

        assertSame(previous, order.getOrderBags());
        assertNotSame(bags, order.getOrderBags());
        assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size());
        assertFalse(order.getOrderBags().contains(oldBag));
    }

    @Test
    void updateWithNewOrderBagsNullOrderBagsTest() {
        Order order = Order.builder().build();
        List<OrderBag> previous = order.getOrderBags();
        List<OrderBag> bags = List.of(
            OrderBag.builder().id(1L).build(),
            OrderBag.builder().id(2L).build(),
            OrderBag.builder().id(3L).build());

        order.updateWithNewOrderBags(bags);

        assertNotNull(order.getOrderBags());
        assertSame(null, previous);
        assertNotSame(bags, order.getOrderBags());
        assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size());

    }

    @Test
    void updateWithNewOrderBagsNullArgExceptionTest() {
        Order order = Order.builder().build();
        List<OrderBag> bags = null;
        assertThrows(NullPointerException.class, () -> order.updateWithNewOrderBags(bags));
    }

    @Test
    void addOrderedBagTest() {
        Order order = new Order();
        List<OrderBag> previous = order.getOrderBags();
        List<OrderBag> bags = List.of(
            OrderBag.builder().id(1L).build(),
            OrderBag.builder().id(2L).build(),
            OrderBag.builder().id(3L).build());

        order.addOrderedBag(bags.get(0));
        order.addOrderedBag(bags.get(1));
        order.addOrderedBag(bags.get(2));

        assertSame(previous, order.getOrderBags());
        assertNotSame(bags, order.getOrderBags());
        assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size());
    }

    @Test
    void removeOrderedBagTest() {
        Order order = new Order();
        List<OrderBag> previous = order.getOrderBags();
        List<OrderBag> bags = new ArrayList<>(List.of(
            OrderBag.builder().id(1L).build(),
            OrderBag.builder().id(2L).build(),
            OrderBag.builder().id(3L).build()));
        order.updateWithNewOrderBags(bags);
        order.removeOrderedBag(bags.get(0));
        bags.remove(bags.get(0));

        assertSame(previous, order.getOrderBags());
        assertNotSame(bags, order.getOrderBags());
        assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size()
            && order.getOrderBags().size() == 2);
    }
}