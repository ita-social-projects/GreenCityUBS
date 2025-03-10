package greencity.mapping.location;

import greencity.dto.CreateAddressRequestDto;
import greencity.dto.location.CoordinatesDto;
import greencity.entity.user.ubs.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateAddressRequestDtoToAddressTest {
    private CreateAddressRequestDtoToAddress converter;

    @BeforeEach
    void setUp() {
        converter = new CreateAddressRequestDtoToAddress();
    }

    @Test
    void convertShouldConvertDtoToAddress() {
        CoordinatesDto coordinatesDto = new CoordinatesDto(50.4501, 30.5234);
        CreateAddressRequestDto dto = CreateAddressRequestDto.builder()
            .districtEn("Shevchenkivskyi")
            .districtUk("Шевченківський")
            .regionEn("Kyiv Oblast")
            .regionUk("Київська область")
            .houseNumber("25B")
            .entranceNumber("3")
            .houseCorpus("2A")
            .addressComment("Next to the park")
            .placeId("ChIJp0lN2HIRkEARuJ1pl_yMcc0")
            .coordinates(coordinatesDto)
            .cityUk("Київ")
            .cityEn("Kyiv")
            .streetUk("Хрещатик")
            .streetEn("Khreshchatyk")
            .build();

        Address address = converter.convert(dto);

        assertEquals(dto.getRegionUk(), address.getAddress().getRegionUk());
        assertEquals(dto.getRegionEn(), address.getAddress().getRegionEn());
        assertEquals(dto.getCityUk(), address.getAddress().getCityUk());
        assertEquals(dto.getCityEn(), address.getAddress().getCityEn());
        assertEquals(dto.getDistrictUk(), address.getAddress().getDistrictUk());
        assertEquals(dto.getDistrictEn(), address.getAddress().getDistrictEn());
        assertEquals(dto.getAddressComment(), address.getAddress().getAddressComment());
        assertEquals(dto.getHouseNumber(), address.getAddress().getHouseNumber());
        assertEquals(dto.getEntranceNumber(), address.getAddress().getEntranceNumber());
        assertEquals(dto.getHouseCorpus(), address.getAddress().getHouseCorpus());
        assertEquals(dto.getStreetUk(), address.getAddress().getStreetUk());
        assertEquals(dto.getStreetEn(), address.getAddress().getStreetEn());
        assertEquals(dto.getCoordinates().getLongitude(), address.getCoordinates().getLongitude());
        assertEquals(dto.getCoordinates().getLatitude(), address.getCoordinates().getLatitude());
    }
}
