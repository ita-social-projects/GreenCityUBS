package greencity.mapping.location;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderAddressToUpdateAddressDtoTest {
    @InjectMocks
    private OrderAddressToUpdateAddressDto mapper;

    @Test
    void convertTest() {
        var orderAddress = ModelUtils.getOrderAddress();

        var result = mapper.convert(orderAddress);

        assertEquals(orderAddress.getId(), result.getOrderAddressExportDetails().getId());
        assertEquals(orderAddress.getBaseAddress().getDistrictUk(),
            result.getOrderAddressExportDetails().getDistrictUk());
        assertEquals(orderAddress.getBaseAddress().getDistrictEn(),
            result.getOrderAddressExportDetails().getDistrictEn());
        assertEquals(orderAddress.getBaseAddress().getStreetUk(), result.getOrderAddressExportDetails().getStreetUk());
        assertEquals(orderAddress.getBaseAddress().getStreetEn(), result.getOrderAddressExportDetails().getStreetEn());
        assertEquals(orderAddress.getBaseAddress().getHouseCorpus(),
            result.getOrderAddressExportDetails().getHouseCorpus());
        assertEquals(orderAddress.getBaseAddress().getEntranceNumber(),
            result.getOrderAddressExportDetails().getEntranceNumber());
        assertEquals(orderAddress.getBaseAddress().getHouseNumber(),
            result.getOrderAddressExportDetails().getHouseNumber());
        assertEquals(orderAddress.getBaseAddress().getCityUk(), result.getOrderAddressExportDetails().getCityUk());
        assertEquals(orderAddress.getBaseAddress().getCityEn(), result.getOrderAddressExportDetails().getCityEn());
        assertEquals(orderAddress.getBaseAddress().getRegionUk(), result.getOrderAddressExportDetails().getRegionUk());
        assertEquals(orderAddress.getBaseAddress().getRegionEn(), result.getOrderAddressExportDetails().getRegionEn());
        assertEquals(orderAddress.getBaseAddress().getAddressComment(),
            result.getOrderAddressExportDetails().getAddressComment());
    }
}
