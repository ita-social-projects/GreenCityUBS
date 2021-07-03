package greencity.mapping;

import greencity.dto.PointsForUbsUserDto;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PointsForUbsUserDtoMapper extends AbstractConverter<Order, PointsForUbsUserDto> {
    /**
     * Method convert {@link Payment} to {@link PointsForUbsUserDto}.
     *
     * @return {@link PointsForUbsUserDto}
     */

    @Override
    protected PointsForUbsUserDto convert(Order order) {
        return PointsForUbsUserDto.builder()
            .dateOfEnrollment(order.getOrderDate())
            .amount(order.getPointsToUse())
            .numberOfOrder(order.getId())
            .build();
    }
}
