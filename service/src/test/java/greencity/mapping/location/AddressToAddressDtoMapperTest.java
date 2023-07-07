package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.mapping.user.AddressToAddressDtoMapper;
import greencity.service.locations.LocationApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AddressToAddressDtoMapperTest {
    @Mock
    LocationApiService locationApiService;
    @InjectMocks
    AddressToAddressDtoMapper addressToAddressDtoMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(locationApiService);
    }

    @Test
    void convert() {
        AddressDto expected = ModelUtils.getAddressDto(1L);
        AddressDto actual = addressToAddressDtoMapper.convert(ModelUtils.getAddress(1L));
        assertEquals(expected.getAddressRegionDistrictList().get(0), actual.getAddressRegionDistrictList().get(0));
    }
}