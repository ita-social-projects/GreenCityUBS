package greencity.entity.order;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class OrderTest {
    @Test
    void updateWithNewOrderBagsTest() {
        Order order = new Order();
        List<OrderBag> previous = order.getOrderBags();
        List<OrderBag> bags = List.of(
            OrderBag.builder().id(1L).build(),
            OrderBag.builder().id(2L).build(),
            OrderBag.builder().id(3L).build());

        order.updateWithNewOrderBags(bags);

        Assertions.assertSame(previous, order.getOrderBags());
        Assertions.assertNotSame(bags, order.getOrderBags());
        Assertions.assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size());
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

        Assertions.assertSame(previous, order.getOrderBags());
        Assertions.assertNotSame(bags, order.getOrderBags());
        Assertions.assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size());
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

        Assertions.assertSame(previous, order.getOrderBags());
        Assertions.assertNotSame(bags, order.getOrderBags());
        Assertions.assertTrue(order.getOrderBags().containsAll(bags) && order.getOrderBags().size() == bags.size()
            && order.getOrderBags().size() == 2);
    }
}