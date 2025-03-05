package greencity.mapping.user;

import greencity.dto.user.PointsForUbsUserDto;
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
        return PointsForUbsUserDto.builder()
            .dateOfEnrollment(changeOfPoints.getDate())
            .amount(changeOfPoints.getAmount())
            .numberOfOrder(changeOfPoints.getOrder().getId())
            .reasonUa(changeOfPoints.getReason().getDescriptionUk())
            .reasonEn(changeOfPoints.getReason().getDescriptionEn())
            .build();
    }
}
