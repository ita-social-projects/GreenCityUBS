package greencity.mapping.employee;

import greencity.ModelUtils;
import greencity.dto.position.PositionWithTranslateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PositionDtoWithTranslateMapperTest {
    @InjectMocks
    PositionDtoWithTranslateMapper positionDtoWithTranslateMapper;

    @Test
    void convert() {
        PositionWithTranslateDto expected = ModelUtils.getPositionWithTranslateDto(1L);
        PositionWithTranslateDto actual = positionDtoWithTranslateMapper.convert(ModelUtils.getPosition());

        assertEquals(expected, actual);
    }
}