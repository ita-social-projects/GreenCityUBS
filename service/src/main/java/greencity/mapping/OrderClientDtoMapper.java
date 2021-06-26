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
        OrderClientDto build = OrderClientDto.builder()
            .id(order.getId())
            .orderStatus(order.getOrderStatus())
            .build();
        if (order.getPayment() == null) {
            build.setAmount(null);
        } else {
            if (order.getPayment().stream().findFirst().isPresent()) {
                build.setAmount(order.getPayment().stream().findFirst().get().getAmount());
            }
        }
        return build;
    }
}