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
        assertEquals(orderAddress.getAddress().getDistrictUk(), result.getOrderAddressExportDetails().getDistrictUk());
        assertEquals(orderAddress.getAddress().getDistrictEn(), result.getOrderAddressExportDetails().getDistrictEn());
        assertEquals(orderAddress.getAddress().getStreetUk(), result.getOrderAddressExportDetails().getStreetUk());
        assertEquals(orderAddress.getAddress().getStreetEn(), result.getOrderAddressExportDetails().getStreetEn());
        assertEquals(orderAddress.getAddress().getHouseCorpus(), result.getOrderAddressExportDetails().getHouseCorpus());
        assertEquals(orderAddress.getAddress().getEntranceNumber(), result.getOrderAddressExportDetails().getEntranceNumber());
        assertEquals(orderAddress.getAddress().getHouseNumber(), result.getOrderAddressExportDetails().getHouseNumber());
        assertEquals(orderAddress.getAddress().getCityUk(), result.getOrderAddressExportDetails().getCityUk());
        assertEquals(orderAddress.getAddress().getCityEn(), result.getOrderAddressExportDetails().getCityEn());
        assertEquals(orderAddress.getAddress().getRegionUk(), result.getOrderAddressExportDetails().getRegionUk());
        assertEquals(orderAddress.getAddress().getRegionEn(), result.getOrderAddressExportDetails().getRegionEn());
        assertEquals(orderAddress.getAddress().getAddressComment(), result.getOrderAddressExportDetails().getAddressComment());
    }
}
