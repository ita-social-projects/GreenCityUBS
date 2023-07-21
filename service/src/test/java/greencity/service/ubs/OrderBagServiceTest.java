package greencity.service.ubs;

import greencity.entity.order.Bag;
import greencity.repository.OrderBagRepository;
import java.util.List;
import static greencity.ModelUtils.getOrderBag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class OrderBagServiceTest {

    @InjectMocks
    private OrderBagService orderBagService;
    @Mock
    private OrderBagRepository orderBagRepository;

    @Test
    public void testFindBagsByOrderId() {
        when(orderBagRepository.findAll()).thenReturn(Arrays.asList(getOrderBag(), getOrderBag2()));
        List<Bag> bags = orderBagService.findBagsByOrderId(1L);
        assertNotNull(bags);
        Bag bag1 = getBag().setFullPrice(getOrderBag().getPrice());
        Bag bag2 = getBag2().setFullPrice(getOrderBag2().getPrice());

        assertEquals(bag1, bags.get(0));
        assertEquals(bag2, bags.get(1));

    }

    @Test
    public void testFindBagsByOrdersList() {
        List<Bag> bags = orderBagService.findAllBagsInOrderBags(Arrays.asList(getOrderBag(), getOrderBag2()));

        Bag bag1 = getBag().setFullPrice(getOrderBag().getPrice());
        Bag bag2 = getBag2().setFullPrice(getOrderBag2().getPrice());
        assertNotNull(bags);
        assertEquals(bag1, bags.get(0));
        assertEquals(bag2, bags.get(1));
    }
}
