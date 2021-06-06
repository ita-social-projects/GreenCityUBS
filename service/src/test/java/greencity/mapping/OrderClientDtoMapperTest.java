package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.OrderClientDto;
import greencity.entity.enums.OrderStatus;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

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
            .payment(new Payment())
            .build();
        order.getPayment().setAmount(350L);

        assertEquals(expected, mapper.convert(order));

        order.setPayment(null);
        expected.setAmount(null);

        assertEquals(expected, mapper.convert(order));
    }
}