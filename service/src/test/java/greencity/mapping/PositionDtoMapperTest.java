package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.PositionDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PositionDtoMapperTest {
    @InjectMocks
    PositionDtoMapper positionDtoMapper;

    @Test
    void convert() {
        PositionDto expected = ModelUtils.getPositionDto();
        PositionDto actual = positionDtoMapper.convert(ModelUtils.getPosition());

        assertEquals(expected, actual);
    }
}
