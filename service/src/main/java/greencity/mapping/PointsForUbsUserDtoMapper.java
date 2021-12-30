package greencity.mapping;

import greencity.dto.PointsForUbsUserDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.entity.order.Payment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class PointsForUbsUserDtoMapper extends AbstractConverter<ChangeOfPoints, PointsForUbsUserDto> {
    /**
     * Method convert {@link Payment} to {@link PointsForUbsUserDto}.
     *
     * @return {@link PointsForUbsUserDto}
     */

    @Override
    protected PointsForUbsUserDto convert(ChangeOfPoints changeOfPoints) {
        Long numberOfOrder = null;
        if (changeOfPoints.getOrder() != null) {
            numberOfOrder = changeOfPoints.getOrder().getId();
        }
        return PointsForUbsUserDto.builder()
            .dateOfEnrollment(changeOfPoints.getDate())
            .amount(changeOfPoints.getAmount())
            .numberOfOrder(numberOfOrder)
            .build();
    }
}
