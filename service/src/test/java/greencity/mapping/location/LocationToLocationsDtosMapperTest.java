package greencity.mapping.location;

import greencity.dto.LocationsDtos;
import greencity.entity.user.Location;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LocationToLocationsDtosMapperTest {

    @InjectMocks
    private LocationToLocationsDtosMapper locationToLocationsDtosMapper;

    @Test
    public void convert() {
        Location expected = Location.builder()
                .id(42L)
                .nameEn("Lviv")
                .nameUk("Львів")
                .build();

        LocationsDtos actual = locationToLocationsDtosMapper.convert(expected);

        assertEquals(expected.getId(), actual.getLocationId());
        assertEquals(expected.getNameEn(), actual.getNameEn());
        assertEquals(expected.getNameUk(), actual.getNameUk());
    }
}
