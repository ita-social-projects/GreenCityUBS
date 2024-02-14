package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.OrderResponseDto;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {
    @InjectMocks
    private OrderMapper orderMapper;

    @Test
    void convert() {
        OrderResponseDto orderResponseDto = ModelUtils.getOrderResponseDto();

        Order expected = Order.builder()
            .additionalOrders(new HashSet<>(List.of("232-534-634")))
            .comment("comment")
            .certificates(Collections.emptySet())
            .pointsToUse(700)
            .build();
        expected.setCertificates(null);

        Order actual = orderMapper.convert(orderResponseDto);
        actual.setOrderDate(null);

        assertEquals(expected, actual);
    }
}
