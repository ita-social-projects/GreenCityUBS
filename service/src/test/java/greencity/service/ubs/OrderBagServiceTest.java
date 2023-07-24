package greencity.service.ubs;

import greencity.entity.order.Bag;
import greencity.entity.order.Order;
import greencity.repository.OrderBagRepository;
import java.util.List;
import static greencity.ModelUtils.getOrderBag;

import greencity.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Optional;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class OrderBagServiceTest {

    @InjectMocks
    private OrderBagService orderBagService;
    @Mock
    private OrderBagRepository orderBagRepository;
    @Mock
    private OrderRepository orderRepository;

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
    void testRemoveBagFromOrder() {
        Order order = getOrder();
        order.setOrderBags(Arrays.asList(getOrderBag(), getOrderBag2()));
        int size = order.getOrderBags().size();
        orderBagService.removeBagFromOrder(order, getOrderBag());
        assertNotEquals(order.getOrderBags().size(), size);
    }
}