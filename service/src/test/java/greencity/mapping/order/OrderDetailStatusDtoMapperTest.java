package greencity.mapping.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import greencity.ModelUtils;
import greencity.dto.order.OrderDetailStatusDto;
import greencity.entity.order.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderDetailStatusDtoMapperTest {
    @InjectMocks
    OrderDetailStatusDtoMapper orderDetailStatusDtoMapper;

    @Test
    void convert() {
        Order order = ModelUtils.getOrder();
        OrderDetailStatusDto expected = OrderDetailStatusDto.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(order.getOrderPaymentStatus().name())
            .date(order.getOrderDate().toLocalDate())
            .build();
        OrderDetailStatusDto actual = orderDetailStatusDtoMapper.convert(order);

        assertEquals(expected, actual);
    }
}
