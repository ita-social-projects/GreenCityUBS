package greencity.mapping.location;

import greencity.ModelUtils;
import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.Region;
import greencity.entity.user.locations.BaseEntityForEnAndUkNames;
import greencity.entity.user.locations.City;
import greencity.entity.user.locations.District;
import greencity.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AddressRequestDtoToBaseEntityMapperTest {
    @InjectMocks
    private AddressRequestDtoToBaseEntityMapper mapper;
    private final CreateAddressRequestDto createdAddressRequestDto = ModelUtils.TEST_CREATE_ADDRESS_DTO;

    @Test
    void cityMapperTest() {
        City result = mapper.convert(createdAddressRequestDto, City.class);

        assertEquals(createdAddressRequestDto.getCity(), result.getNameUk());
        assertEquals(createdAddressRequestDto.getCityEn(), result.getNameEn());
    }

    @Test
    void regionMapperTest() {
        Region result = mapper.convert(createdAddressRequestDto, Region.class);

        assertEquals(createdAddressRequestDto.getRegion(), result.getNameUk());
        assertEquals(createdAddressRequestDto.getRegionEn(), result.getNameEn());
    }

    @Test
    void districtMapperTest() {
        District result = mapper.convert(createdAddressRequestDto, District.class);

        assertEquals(createdAddressRequestDto.getDistrict(), result.getNameUk());
        assertEquals(createdAddressRequestDto.getDistrictEn(), result.getNameEn());
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Integer.class})
    <T extends BaseEntityForEnAndUkNames> void invalidDataTest(Class<T> clazz) {

        assertThrows(BadRequestException.class, () -> mapper.convert(createdAddressRequestDto, clazz));
    }
}
