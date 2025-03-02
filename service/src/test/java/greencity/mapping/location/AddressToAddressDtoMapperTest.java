package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.mapping.user.AddressToAddressDtoMapper;
import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressToAddressDtoMapperTest {

    @Mock
    private LocationApiService locationApiService;

    @InjectMocks
    private AddressToAddressDtoMapper addressToAddressDtoMapper;

    @Test
    void convert() {
        AddressDto expected = ModelUtils.getAddressDto(1L);
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString()))
            .thenReturn(ModelUtils.getLocationApiDtoList());
        AddressDto actual = addressToAddressDtoMapper.convert(ModelUtils.getAddress(1L));
        assertEquals(expected.getAddressRegionDistrictList().size(), actual.getAddressRegionDistrictList().size());
    }
}
