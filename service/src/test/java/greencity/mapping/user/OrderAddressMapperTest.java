package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.entity.user.ubs.Address;
import greencity.entity.user.ubs.OrderAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderAddressMapperTest {

    @InjectMocks
    private OrderAddressMapper orderAddressMapper;

    @Test
    void convert() {
        Address address = ModelUtils.getAddress();
        OrderAddress orderAddress = ModelUtils.getOrderAddress();
        OrderAddress expectedOrderAddressResult = orderAddressMapper.convert(address);

        assertEquals(expectedOrderAddressResult.getId(), orderAddress.getId());
        assertEquals(expectedOrderAddressResult.getCity(), orderAddress.getCity());
        assertEquals(expectedOrderAddressResult.getStreet(), orderAddress.getStreet());
        assertEquals(expectedOrderAddressResult.getDistrict(), orderAddress.getDistrict());
        assertEquals(expectedOrderAddressResult.getEntranceNumber(), orderAddress.getEntranceNumber());
        assertEquals(expectedOrderAddressResult.getHouseNumber(), orderAddress.getHouseNumber());
        assertEquals(expectedOrderAddressResult.getHouseCorpus(), orderAddress.getHouseCorpus());
        assertEquals(expectedOrderAddressResult.getCoordinates(), orderAddress.getCoordinates());
        assertEquals(expectedOrderAddressResult.getActual(), orderAddress.getActual());
        assertEquals(expectedOrderAddressResult.getAddressStatus(), orderAddress.getAddressStatus());
    }

}
