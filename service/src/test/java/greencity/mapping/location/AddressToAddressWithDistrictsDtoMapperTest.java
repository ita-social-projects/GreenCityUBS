package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressWithDistrictsDto;
import greencity.mapping.user.AddressToAddressWithDistrictsDtoMapper;
import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressToAddressDtoMapperTest {

    @Mock
    private LocationApiService locationApiService;

    @InjectMocks
    private AddressToAddressWithDistrictsDtoMapper addressToAddressWithDistrictsDtoMapper;

    @Test
    void convert() {
        MockitoAnnotations.initMocks(this);
        AddressWithDistrictsDto expected = ModelUtils.getAddressWithDistrictsDto(1L);
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString()))
            .thenReturn(ModelUtils.getLocationApiDtoList());
        AddressWithDistrictsDto actual = addressToAddressWithDistrictsDtoMapper.convert(ModelUtils.getAddress(1L));
        assertEquals(expected.getAddressRegionDistrictList().size(), actual.getAddressRegionDistrictList().size());
        assertEquals(expected.getAddressRegionDistrictList().get(0), actual.getAddressRegionDistrictList().get(0));

    }
}
