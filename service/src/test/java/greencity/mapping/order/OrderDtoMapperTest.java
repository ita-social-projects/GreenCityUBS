package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.OrderClientForDto;
import greencity.enums.OrderStatus;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class OrderDtoMapperTest {

    @InjectMocks
    OrderDtoMapper orderDtoMapper;

    @Test
    void convert() {

        Order order = ModelUtils.getOrder2();

        Map<Integer, Integer> amountOfBagsOrdered = new HashMap<>();
        amountOfBagsOrdered.put(1, 1);
        order.setAmountOfBagsOrdered(amountOfBagsOrdered);
        order.setOrderStatus(OrderStatus.CANCELED);

        OrderClientForDto expected = OrderClientForDto.builder()
            .id(1L)
            .counter(null)
            .orderStatus(OrderStatus.CANCELED)
            .amount(new ArrayList<>(order.getAmountOfBagsOrdered().keySet()))
            .build();

        OrderClientForDto actual = orderDtoMapper.convert(order);

        Assertions.assertEquals(expected, actual);

    }
}
