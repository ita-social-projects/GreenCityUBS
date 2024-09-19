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
        source.setRegion("Lviv");
        source.setRegionEn("Lviv Region");
        source.setCity("Lviv");
        source.setCityEn("Lviv City");
        source.setDistrict("Sykhiv");
        source.setDistrictEn("Sykhiv District");

        CreateAddressRequestDto result = converter.convert(source);

        assertEquals("Lviv", result.getRegion());
        assertEquals("Lviv Region", result.getRegionEn());
        assertEquals("Lviv", result.getCity());
        assertEquals("Lviv City", result.getCityEn());
        assertEquals("Sykhiv", result.getDistrict());
        assertEquals("Sykhiv District", result.getDistrictEn());
    }
}
