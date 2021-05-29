package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderResponseDto;
import greencity.entity.order.Order;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {
    @InjectMocks
    private OrderMapper orderMapper;

    @Test
    void convert() {
        OrderResponseDto orderResponseDto = ModelUtils.getOrderResponseDto();

        Order expected = Order.builder()
            .additionalOrders(new HashSet<>(Arrays.asList("232-534-634")))
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
