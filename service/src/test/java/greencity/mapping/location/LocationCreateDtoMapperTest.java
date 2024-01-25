package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.location.LocationCreateDto;
import greencity.entity.user.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LocationCreateDtoMapperTest {
    @InjectMocks
    private LocationCreateDtoMapper mapper;

    @Test
    void convert() {
        Location location = ModelUtils.getLocation();
        List<LocationCreateDto> dtoList = ModelUtils.getLocationCreateDtoList();
        assertEquals(dtoList.get(0).getAddLocationDtoList().get(0).getLocationName(),
            mapper.convert(location).getAddLocationDtoList().get(0).getLocationName());
        assertEquals(dtoList.get(0).getAddLocationDtoList().get(0).getLanguageCode(),
            mapper.convert(location).getAddLocationDtoList().get(0).getLanguageCode());
        assertEquals(dtoList.get(0).getRegionTranslationDtos().get(0).getRegionName(),
            mapper.convert(location).getRegionTranslationDtos().get(0).getRegionName());
        assertEquals(dtoList.get(0).getRegionTranslationDtos().get(0).getLanguageCode(),
            mapper.convert(location).getRegionTranslationDtos().get(0).getLanguageCode());
        assertEquals(dtoList.get(0).getLatitude(), mapper.convert(location).getLatitude());
        assertEquals(dtoList.get(0).getLongitude(), mapper.convert(location).getLongitude());
    }
}
