package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.locations.District;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreateAddressRequestDtoToDistrictTest {
    private final CreateAddressRequestDtoToDistrict converter = new CreateAddressRequestDtoToDistrict();

    @Test
    void convertShouldMapDtoToDistrict() {
        CreateAddressRequestDto dto = CreateAddressRequestDto.builder()
            .district("Шевченківський")
            .districtEn("Shevchenkivskyi")
            .build();

        District district = converter.convert(dto);

        assertNotNull(district);
        assertEquals("Шевченківський", district.getNameUk());
        assertEquals("Shevchenkivskyi", district.getNameEn());
    }

    @Test
    void convert_ShouldHandleNullValues() {
        CreateAddressRequestDto dto = CreateAddressRequestDto.builder()
            .district(null)
            .districtEn(null)
            .build();

        District district = converter.convert(dto);

        assertNotNull(district);
        assertNull(district.getNameUk());
        assertNull(district.getNameEn());
    }
}
