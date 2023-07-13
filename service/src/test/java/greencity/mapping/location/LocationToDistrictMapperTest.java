package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.mapping.location.api.LocationToDistrictDtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class LocationToDistrictMapperTest {
    @InjectMocks
    private LocationToDistrictDtoMapper locationToDistrictDtoMapper;

    @Test
    void convert() {
        DistrictDto expected = ModelUtils.getDistrictDto();
        DistrictDto actual = locationToDistrictDtoMapper.convert(ModelUtils.getLocationApiDto());
        assertEquals(expected, actual);
    }
}