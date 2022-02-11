package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PointsForUbsUserDto;
import greencity.entity.order.ChangeOfPoints;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointsForUbsUserDtoMapperTest {

    @InjectMocks
    private PointsForUbsUserDtoMapper pointsForUbsUserDtoMapper;

    @Test
    void convert() {

        ChangeOfPoints changeOfPoints = ModelUtils.getChangeOfPoints();
        changeOfPoints.setDate(null);

        PointsForUbsUserDto expected = PointsForUbsUserDto.builder()
            .dateOfEnrollment(null)
            .numberOfOrder(1L)
            .amount(0)
            .build();

        PointsForUbsUserDto actual = pointsForUbsUserDtoMapper.convert(changeOfPoints);

        Assertions.assertEquals(actual.toString(), expected.toString());

    }
}
