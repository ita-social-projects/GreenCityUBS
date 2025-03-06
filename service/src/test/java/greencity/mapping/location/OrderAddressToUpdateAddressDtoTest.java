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
        assertEquals(orderAddress.getDistrictUk(), result.getOrderAddressExportDetails().getDistrictUk());
        assertEquals(orderAddress.getDistrictEn(), result.getOrderAddressExportDetails().getDistrictEn());
        assertEquals(orderAddress.getStreetUk(), result.getOrderAddressExportDetails().getStreetUk());
        assertEquals(orderAddress.getStreetEn(), result.getOrderAddressExportDetails().getStreetEn());
        assertEquals(orderAddress.getHouseCorpus(), result.getOrderAddressExportDetails().getHouseCorpus());
        assertEquals(orderAddress.getEntranceNumber(), result.getOrderAddressExportDetails().getEntranceNumber());
        assertEquals(orderAddress.getHouseNumber(), result.getOrderAddressExportDetails().getHouseNumber());
        assertEquals(orderAddress.getCityUk(), result.getOrderAddressExportDetails().getCityUk());
        assertEquals(orderAddress.getCityEn(), result.getOrderAddressExportDetails().getCityEn());
        assertEquals(orderAddress.getRegionUk(), result.getOrderAddressExportDetails().getRegionUk());
        assertEquals(orderAddress.getRegionEn(), result.getOrderAddressExportDetails().getRegionEn());
        assertEquals(orderAddress.getAddressComment(), result.getOrderAddressExportDetails().getAddressComment());
    }
}
