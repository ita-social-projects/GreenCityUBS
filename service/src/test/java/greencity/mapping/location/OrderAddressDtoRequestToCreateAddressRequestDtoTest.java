package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.order.OrderAddressDtoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderAddressDtoRequestToCreateAddressRequestDtoTest {
    private OrderAddressDtoRequestToCreateAddressRequestDto converter;

    @BeforeEach
    void setUp() {
        converter = new OrderAddressDtoRequestToCreateAddressRequestDto();
    }

    @Test
    void testConvert() {
        OrderAddressDtoRequest source = new OrderAddressDtoRequest();
        source.setRegionUk("Lviv");
        source.setRegionEn("Lviv Region");
        source.setCityUk("Lviv");
        source.setCityEn("Lviv City");
        source.setDistrictUk("Sykhiv");
        source.setDistrictEn("Sykhiv District");

        CreateAddressRequestDto result = converter.convert(source);

        assertEquals("Lviv", result.getRegionUk());
        assertEquals("Lviv Region", result.getRegionEn());
        assertEquals("Lviv", result.getCityUk());
        assertEquals("Lviv City", result.getCityEn());
        assertEquals("Sykhiv", result.getDistrictUk());
        assertEquals("Sykhiv District", result.getDistrictEn());
    }
}
