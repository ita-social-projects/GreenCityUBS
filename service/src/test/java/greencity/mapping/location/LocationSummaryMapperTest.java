package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.location.LocationSummaryDto;
import greencity.entity.user.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationSummaryMapperTest {
    @InjectMocks
    private LocationSummaryMapper mapper;

    @Test
    void convert() {
        Region region = ModelUtils.getRegionForSummary();
        LocationSummaryDto dto = ModelUtils.getInfoAboutLocationSummaryDto();
        Assertions.assertEquals(dto, mapper.convert(region));
    }
}
