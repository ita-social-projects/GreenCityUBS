package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderDto;
import greencity.entity.order.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderMapperDtoTest {
    @InjectMocks
    OrderMapperDto orderMapperDto;

    @Test
    void convert() {
        OrderDto expected = ModelUtils.getOrderDto();
        Order order = ModelUtils.getOrder();

        assertEquals(expected, orderMapperDto.convert(order));
    }
}
