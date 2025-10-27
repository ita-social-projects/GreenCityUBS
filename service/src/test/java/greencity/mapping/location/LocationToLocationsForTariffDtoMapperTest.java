package greencity.mapping.location;

import greencity.dto.location.LocationsForTariffDto;
import greencity.entity.user.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static greencity.ModelUtils.getLocation;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LocationToLocationsForTariffDtoMapperTest {

    @InjectMocks
    private LocationToLocationsForTariffDtoMapper locationToLocationsForTariffDtoMapper;

    @Test
    void convert() {
        Location expected = getLocation();

        LocationsForTariffDto actual = locationToLocationsForTariffDtoMapper.convert(expected);

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLocationStatus().toString(), actual.getLocationStatus());
        assertEquals(expected.getNameEn(), actual.getNameEn());
        assertEquals(expected.getNameUk(), actual.getNameUk());
        assertEquals(expected.getCoordinates().getLatitude(), actual.getLatitude());
        assertEquals(expected.getCoordinates().getLongitude(), actual.getLongitude());
        assertEquals(expected.getRegion().getNameEn(), actual.getRegionNameEn());
        assertEquals(expected.getRegion().getNameUk(), actual.getRegionNameUk());
    }
}