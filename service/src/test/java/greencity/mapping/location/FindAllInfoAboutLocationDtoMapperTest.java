package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.location.LocationInfoDto;
import greencity.entity.user.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindAllInfoAboutLocationDtoMapperTest {
    @InjectMocks
    private FindAllInfoAboutLocationDtoMapper mapper;

    @Test
    void convert() {
        Region region = ModelUtils.getRegion();
        LocationInfoDto dto = ModelUtils.getInfoAboutLocationDto();
        Assertions.assertEquals(dto.getRegionId(), mapper.convert(region).getRegionId());
        Assertions.assertEquals(dto.getRegionTranslationDtos(), mapper.convert(region).getRegionTranslationDtos());
    }
}
