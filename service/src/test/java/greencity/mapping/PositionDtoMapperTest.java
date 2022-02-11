package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PositionDto;
import greencity.entity.user.employee.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PositionDtoMapperTest {

    @InjectMocks
    PositionDtoMapper positionDtoMapper;

    @Test
    void convert() {

        Position position = ModelUtils.getPosition();

        PositionDto expected = PositionDto.builder()
            .id(1l)
            .name("Водій")
            .build();

        PositionDto actual = positionDtoMapper.convert(position);
        Assertions.assertEquals(actual, expected);

    }

}
