package greencity.mapping.location;

import greencity.dto.LocationsDtos;
import greencity.entity.user.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static greencity.ModelUtils.getLocation;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LocationToLocationsDtosMapperTest {

    @InjectMocks
    private LocationToLocationsDtosMapper locationToLocationsDtosMapper;

    @Before
    public void setup() {
        locationToLocationsDtosMapper = new LocationToLocationsDtosMapper();
    }

    @Test
    public void convert() {
        Location expected = getLocation();

        LocationsDtos actual = locationToLocationsDtosMapper.convert(expected);

        assertEquals(expected.getId(), actual.getLocationId());
        assertEquals(expected.getNameEn(), actual.getNameEn());
        assertEquals(expected.getNameUk(), actual.getNameUk());
    }
}
