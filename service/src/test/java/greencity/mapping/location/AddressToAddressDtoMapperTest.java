package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.dto.location.api.DistrictDto;
import greencity.entity.user.locations.District;
import greencity.entity.user.ubs.Address;
import greencity.mapping.user.AddressToAddressDtoMapper;
import greencity.repository.CityRepository;
import greencity.repository.DistrictRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressToAddressDtoMapperTest {
    @Mock
    private CityRepository cityRepository;
    @Mock
    private DistrictRepository districtRepository;

    @InjectMocks
    private AddressToAddressDtoMapper addressToAddressDtoMapper;

    @Test
    void convert() {
        Address address = ModelUtils.getAddress(1L);
        List<District> districts = List.of(
            District.builder()
                .id(1L)
                .nameEn("Test")
                .build());
        Long cityId = 10L;
        when(cityRepository.findIdByNameUkOrNameEn(anyString())).thenReturn(cityId);
        when(districtRepository.findAllByCityId(cityId)).thenReturn(districts);
        AddressDto actual = addressToAddressDtoMapper.convert(address);

        assertEquals(address.getId(), actual.getId());
        assertEquals(1, actual.getAddressRegionDistrictList().size());

        DistrictDto dto1 = actual.getAddressRegionDistrictList().get(0);
        assertEquals("Test", dto1.getNameEn());
    }
}
