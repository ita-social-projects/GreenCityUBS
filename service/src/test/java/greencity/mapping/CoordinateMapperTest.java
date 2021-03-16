package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.CoordinatesDto;
import greencity.entity.coords.Coordinates;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CoordinateMapperTest {
    @InjectMocks
    CoordinateMapper coordinateMapper;

    @Test
    void convert() {
        Coordinates expected = ModelUtils.getCoordinates();
        CoordinatesDto dto = ModelUtils.getCoordinatesDto();

        assertEquals(expected, coordinateMapper.convert(dto));
    }
}
