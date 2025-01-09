package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.mapping.user.AddressToAddressDtoMapper;
import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressToAddressDtoMapperTest {
    private static final LocationApiService locationApiService =
        Mockito.mock(LocationApiService.class);
    private static final AddressToAddressDtoMapper addressToAddressDtoMapper =
        new AddressToAddressDtoMapper(locationApiService);

    private static final AddressDto expected = ModelUtils.getAddressDto(1L);
    private static final AddressDto actual;

    static {
        when(locationApiService.getAllDistrictsInCityByNames(anyString(), anyString()))
            .thenReturn(ModelUtils.getLocationApiDtoList());

        actual = addressToAddressDtoMapper.convert(ModelUtils.getAddress(1L));
    }

    @Test
    void convert() {
        assertEquals(expected.getAddressRegionDistrictList().size(), actual.getAddressRegionDistrictList().size());
    }
}
