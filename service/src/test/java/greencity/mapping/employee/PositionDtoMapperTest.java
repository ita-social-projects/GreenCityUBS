package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.position.PositionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PositionDtoMapperTest {
    @InjectMocks
    PositionDtoMapper positionDtoMapper;

    @Test
    void convert() {
        PositionDto expected = ModelUtils.getPositionDto(1L);
        PositionDto actual = positionDtoMapper.convert(ModelUtils.getPosition());

        assertEquals(expected, actual);
    }
}
