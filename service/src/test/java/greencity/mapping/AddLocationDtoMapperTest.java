package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.AddLocationDto;
import greencity.entity.user.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AddLocationDtoMapperTest {
    @InjectMocks
    private AddLocationDtoMapper mapper;

    @Test
    void convert() {
        Location location = ModelUtils.getLocation();
        location.setLocationTranslations(ModelUtils.getLocationTranslationList());
        AddLocationDto dto = ModelUtils.getAddLocationDto();
        assertEquals(dto.getAddLocationDtoList().get(0).getLocationName(),
            mapper.convert(location).getAddLocationDtoList().get(0).getLocationName());
        assertEquals(dto.getAddLocationDtoList().get(0).getLanguageId(),
            mapper.convert(location).getAddLocationDtoList().get(0).getLanguageId());
        assertEquals(dto.getAddLocationDtoList().get(0).getRegion(),
            mapper.convert(location).getAddLocationDtoList().get(0).getRegion());
    }
}
