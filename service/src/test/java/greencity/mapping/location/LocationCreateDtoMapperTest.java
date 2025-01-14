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
        assertEquals(dtoList.getFirst().getAddLocationDtoList().getFirst().getLocationName(),
            mapper.convert(location).getAddLocationDtoList().getFirst().getLocationName());
        assertEquals(dtoList.getFirst().getAddLocationDtoList().getFirst().getLanguageCode(),
            mapper.convert(location).getAddLocationDtoList().getFirst().getLanguageCode());
        assertEquals(dtoList.getFirst().getRegionTranslationDtos().getFirst().getRegionName(),
            mapper.convert(location).getRegionTranslationDtos().getFirst().getRegionName());
        assertEquals(dtoList.getFirst().getRegionTranslationDtos().getFirst().getLanguageCode(),
            mapper.convert(location).getRegionTranslationDtos().getFirst().getLanguageCode());
        assertEquals(dtoList.getFirst().getLatitude(), mapper.convert(location).getLatitude());
        assertEquals(dtoList.getFirst().getLongitude(), mapper.convert(location).getLongitude());
    }
}
