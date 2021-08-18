package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.CoordinatesDto;
import greencity.entity.coords.Coordinates;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
