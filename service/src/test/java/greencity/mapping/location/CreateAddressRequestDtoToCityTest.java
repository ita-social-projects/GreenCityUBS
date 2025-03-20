package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.entity.user.locations.City;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreateAddressRequestDtoToCityTest {
    private final CreateAddressRequestDtoToCity converter = new CreateAddressRequestDtoToCity();

    @Test
    void convertShouldMapDtoToCity() {
        CreateAddressRequestDto dto = CreateAddressRequestDto.builder()
            .city("Київ")
            .cityEn("Kyiv")
            .build();

        City city = converter.convert(dto);

        assertNotNull(city);
        assertEquals("Київ", city.getNameUk());
        assertEquals("Kyiv", city.getNameEn());
    }

    @Test
    void convertShouldHandleNullValues() {
        CreateAddressRequestDto dto = CreateAddressRequestDto.builder()
            .city(null)
            .cityEn(null)
            .build();

        City city = converter.convert(dto);

        assertNotNull(city);
        assertNull(city.getNameUk());
        assertNull(city.getNameEn());
    }
}
