package greencity.mapping.order;

import greencity.ModelUtils;
import greencity.dto.order.OrderClientDto;
import greencity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderClientDtoMapperTest {

    @InjectMocks
    OrderClientDtoMapper mapper;

    @Test
    void convert() {
        OrderClientDto expected = ModelUtils.getOrderClientDto();
        Order order = Order.builder()
            .id(1L)
            .orderStatus(OrderStatus.DONE)
            .payment(Collections.singletonList(new Payment()))
            .build();
        order.getPayment().getFirst().setAmount(350L);

        assertEquals(expected, mapper.convert(order));

        order.setPayment(null);
        expected.setAmount(0L);

        assertEquals(expected, mapper.convert(order));
    }
}