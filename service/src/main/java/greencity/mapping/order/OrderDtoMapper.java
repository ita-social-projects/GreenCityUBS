package greencity.mapping.order;

import greencity.dto.order.OrderClientForDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDtoMapper extends AbstractConverter<Order, OrderClientForDto> {
    @Override
    protected OrderClientForDto convert(Order order) {
        List<Integer> amountList = new ArrayList<>(order.getAmountOfBagsOrdered().keySet());

        return OrderClientForDto.builder()
            .id(order.getId())
            .counter(order.getCounterOrderPaymentId())
            .orderStatus(order.getOrderStatus())
            .amount(amountList)
            .build();
    }
}
