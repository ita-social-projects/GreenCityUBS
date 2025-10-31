package greencity.mapping.order;

import greencity.dto.order.OrderDetailStatusDto;
import greencity.entity.order.Order;
import greencity.enums.PaymentStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailStatusDtoMapper extends AbstractConverter<Order, OrderDetailStatusDto> {
    @Override
    protected OrderDetailStatusDto convert(Order order) {
        return OrderDetailStatusDto.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus().name())
            .paymentStatus(order.getOrderPaymentStatus().name())
            .date(order.getOrderDate().toLocalDate())
            .build();
    }
}
