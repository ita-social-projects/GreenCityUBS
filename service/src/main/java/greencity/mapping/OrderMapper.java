package greencity.mapping;

import greencity.dto.OrderResponseDto;
import greencity.entity.order.Order;
import java.time.LocalDateTime;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link OrderResponseDto} into
 * {@link Order}.
 */
@Component
public class OrderMapper extends AbstractConverter<OrderResponseDto, Order> {
    /**
     * Method convert {@link OrderResponseDto} to {@link Order}.
     *
     * @return {@link Order}
     */
    @Override
    protected Order convert(OrderResponseDto dto) {
        return Order.builder()
            .orderDate(LocalDateTime.now())
            .pointsToUse(dto.getPointsToUse())
            .comment(dto.getOrderComment())
            .additionalOrders(dto.getAdditionalOrders())
            .build();
    }
}
