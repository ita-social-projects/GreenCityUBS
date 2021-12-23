package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.FindInfoAboutLocationDto;
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
        FindInfoAboutLocationDto dto = ModelUtils.getInfoAboutLocationDto();
        Assertions.assertEquals(dto, mapper.convert(region));
    }
}
