package greencity.mapping;

import greencity.dto.OrderClientDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class OrderClientDtoMapper extends AbstractConverter<Order, OrderClientDto> {
    /**
     * Method convert {@link Order} to {@link OrderClientDto}.
     *
     * @return {@link OrderClientDto}
     */
    @Override
    protected OrderClientDto convert(Order order) {
        return OrderClientDto.builder()
                .id(order.getId())
                .orderStatus(order.getOrderStatus())
                .amount(order.getPayment().getAmount())
                .build();
    }
}
