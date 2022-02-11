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
public class PositionMapperTest {

    @InjectMocks
    PositionMapper positionMapper;

    @Test
    void convert() {

        Position expected = ModelUtils.getPosition();

        PositionDto positionDto = PositionDto.builder()
            .id(1L)
            .name("Водій")
            .build();

        Position actual = positionMapper.convert(positionDto);

        Assertions.assertEquals(actual, expected);

    }
}
