package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderDto;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderMapperDtoTest {
    @InjectMocks
    OrderMapperDto orderMapperDto;

    @Test
    void convert() {
        OrderDto expected = ModelUtils.getOrderDto();
        Order order = ModelUtils.getOrder();

        assertEquals(expected, orderMapperDto.convert(order));
    }
}
