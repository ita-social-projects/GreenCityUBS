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
        assertEquals(orderAddress.getDistrict(), result.getOrderAddressExportDetails().getDistrict());
        assertEquals(orderAddress.getDistrictEn(), result.getOrderAddressExportDetails().getDistrictEn());
        assertEquals(orderAddress.getStreet(), result.getOrderAddressExportDetails().getStreet());
        assertEquals(orderAddress.getStreetEn(), result.getOrderAddressExportDetails().getStreetEn());
        assertEquals(orderAddress.getHouseCorpus(), result.getOrderAddressExportDetails().getHouseCorpus());
        assertEquals(orderAddress.getEntranceNumber(), result.getOrderAddressExportDetails().getEntranceNumber());
        assertEquals(orderAddress.getHouseNumber(), result.getOrderAddressExportDetails().getHouseNumber());
        assertEquals(orderAddress.getCity(), result.getOrderAddressExportDetails().getCity());
        assertEquals(orderAddress.getCityEn(), result.getOrderAddressExportDetails().getCityEn());
        assertEquals(orderAddress.getRegion(), result.getOrderAddressExportDetails().getRegion());
        assertEquals(orderAddress.getRegionEn(), result.getOrderAddressExportDetails().getRegionEn());
        assertEquals(orderAddress.getAddressComment(), result.getOrderAddressExportDetails().getAddressComment());
    }
}
