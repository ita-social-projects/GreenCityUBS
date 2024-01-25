package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.user.PointsForUbsUserDto;
import greencity.entity.order.ChangeOfPoints;
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
        PointsForUbsUserDto expected = PointsForUbsUserDto.builder()
            .numberOfOrder(changeOfPoints.getOrder().getId())
            .amount(changeOfPoints.getAmount())
            .dateOfEnrollment(changeOfPoints.getDate())
            .build();
        PointsForUbsUserDto actual = pointsForUbsUserDtoMapper.convert(changeOfPoints);

        assertEquals(expected, actual);
    }
}
