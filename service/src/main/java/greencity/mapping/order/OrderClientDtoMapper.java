package greencity.mapping.order;

import greencity.dto.order.OrderClientDto;
import greencity.entity.order.Order;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.LongStream;

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
            .amount(Optional.ofNullable(order.getPayment())
                .stream().flatMap(Collection::stream)
                .flatMapToLong(payment -> LongStream.of(payment.getAmount()))
                .reduce(Long::sum).orElse(0L))
            .build();
    }
}