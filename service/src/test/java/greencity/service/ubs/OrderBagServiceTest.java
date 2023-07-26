package greencity.service.ubs;

import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.entity.order.OrderBag;
import greencity.repository.OrderBagRepository;

import java.util.*;

import static greencity.ModelUtils.getOrderBag;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class OrderBagServiceTest {

    @InjectMocks
    private OrderBagService orderBagService;
    @Mock
    private OrderBagRepository orderBagRepository;

    @Test
    void testFindBagsByOrderId() {
        when(orderBagRepository.findOrderBagsByOrderId(any())).thenReturn(Arrays.asList(getOrderBag(), getOrderBag2()));
        List<Bag> bags = orderBagService.findBagsByOrderId(1L);
        assertNotNull(bags);
        Bag bag1 = getBag().setFullPrice(getOrderBag().getPrice());
        Bag bag2 = getBag2().setFullPrice(getOrderBag2().getPrice());

        assertEquals(bag1, bags.get(0));
        assertEquals(bag2, bags.get(1));

    }

    @Test
    void testFindBagsByOrdersList() {
        List<Bag> bags = orderBagService.findAllBagsInOrderBagsList(Arrays.asList(getOrderBag(), getOrderBag2()));

        Bag bag1 = getBag().setFullPrice(getOrderBag().getPrice());
        Bag bag2 = getBag2().setFullPrice(getOrderBag2().getPrice());
        assertNotNull(bags);
        assertEquals(bag1, bags.get(0));
        assertEquals(bag2, bags.get(1));
    }

    @Test
    void testGetActualBagsAmountForOrder_WithExportedQuantity() {
        List<OrderBag> bagsForOrder = new ArrayList<>();
        OrderBag bag1 = createOrderBagWithExportedQuantity(1, 10);
        OrderBag bag2 = createOrderBagWithExportedQuantity(2, 20);
        bagsForOrder.add(bag1);
        bagsForOrder.add(bag2);

        Map<Integer, Integer> result = orderBagService.getActualBagsAmountForOrder(bagsForOrder);

        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(1, 10);
        expected.put(2, 20);
        assertEquals(expected, result);
    }

    @Test
    void testGetActualBagsAmountForOrder_WithConfirmedQuantity() {
        List<OrderBag> bagsForOrder = new ArrayList<>();
        OrderBag bag1 = createOrderBagWithConfirmedQuantity(1, 5);
        OrderBag bag2 = createOrderBagWithConfirmedQuantity(2, 15);
        bagsForOrder.add(bag1);
        bagsForOrder.add(bag2);

        Map<Integer, Integer> result = orderBagService.getActualBagsAmountForOrder(bagsForOrder);

        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(1, 5);
        expected.put(2, 15);
        assertEquals(expected, result);
    }

    @Test
    void testGetActualBagsAmountForOrder_WithAmount() {
        List<OrderBag> bagsForOrder = new ArrayList<>();
        OrderBag bag1 = createOrderBagWithAmount(1, 3);
        OrderBag bag2 = createOrderBagWithAmount(2, 7);
        bagsForOrder.add(bag1);
        bagsForOrder.add(bag2);

        Map<Integer, Integer> result = orderBagService.getActualBagsAmountForOrder(bagsForOrder);

        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(1, 3);
        expected.put(2, 7);
        assertEquals(expected, result);
    }

    @Test
    void testGetActualBagsAmountForOrder_WithNoMatch() {
        List<OrderBag> bagsForOrder = new ArrayList<>();
        OrderBag bag1 = createOrderBagWithAmount(1, 3);
        bag1.setExportedQuantity(null);
        bag1.setConfirmedQuantity(null);
        bag1.setAmount(null);

        OrderBag bag2 = createOrderBagWithAmount(2, 7);
        bag2.setExportedQuantity(null);
        bag2.setConfirmedQuantity(null);
        bag2.setAmount(null);
        bagsForOrder.add(bag1);
        bagsForOrder.add(bag2);

        Map<Integer, Integer> result = orderBagService.getActualBagsAmountForOrder(bagsForOrder);

        Map<Integer, Integer> expected = new HashMap<>();
        assertEquals(expected, result);
    }

    private OrderBag createOrderBagWithExportedQuantity(int bagId, int exportedQuantity) {
        OrderBag orderBag = new OrderBag();
        Bag bag = new Bag();
        bag.setId(bagId);
        orderBag.setBag(bag);
        orderBag.setExportedQuantity(exportedQuantity);
        return orderBag;
    }

    private OrderBag createOrderBagWithConfirmedQuantity(int bagId, int confirmedQuantity) {
        OrderBag orderBag = new OrderBag();
        Bag bag = new Bag();
        bag.setId(bagId);
        orderBag.setBag(bag);
        orderBag.setConfirmedQuantity(confirmedQuantity);
        return orderBag;
    }

    private OrderBag createOrderBagWithAmount(int bagId, int amount) {
        OrderBag orderBag = new OrderBag();
        Bag bag = new Bag();
        bag.setId(bagId);
        orderBag.setBag(bag);
        orderBag.setAmount(amount);
        return orderBag;
    }
}