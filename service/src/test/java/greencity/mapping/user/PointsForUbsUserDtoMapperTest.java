package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.PointsForUbsUserDto;
import greencity.entity.order.ChangeOfPoints;
import greencity.enums.BonusReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PointsForUbsUserDtoMapperTest {
    @InjectMocks
    PointsForUbsUserDtoMapper pointsForUbsUserDtoMapper;

    @Test
    void convert() {
        ChangeOfPoints changeOfPoints = ModelUtils.getChangeOfPoints();
        changeOfPoints.setReason(BonusReason.RETURN_OVERPAY);
        PointsForUbsUserDto expected = PointsForUbsUserDto.builder()
            .numberOfOrder(changeOfPoints.getOrder().getId())
            .amount(changeOfPoints.getAmount())
            .dateOfEnrollment(changeOfPoints.getDate())
            .reasonUk(changeOfPoints.getReason().getDescriptionUk())
            .reasonEn(changeOfPoints.getReason().getDescriptionEn())
            .build();
        PointsForUbsUserDto actual = pointsForUbsUserDtoMapper.convert(changeOfPoints);

        assertEquals(expected, actual);
    }
}
