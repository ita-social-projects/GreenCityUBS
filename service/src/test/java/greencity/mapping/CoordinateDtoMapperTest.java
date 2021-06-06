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
public class CoordinateDtoMapperTest {

    @InjectMocks
    CoordinateDtoMapper coordinateDtoMapper;

    @Test
    void convert() {
        CoordinatesDto expected = ModelUtils.getCoordinatesDto();
        Coordinates coordinates = ModelUtils.getCoordinates();

        assertEquals(expected, coordinateDtoMapper.convert(coordinates));
    }
}
