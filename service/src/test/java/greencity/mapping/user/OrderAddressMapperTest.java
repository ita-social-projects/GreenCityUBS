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
        assertEquals(expectedOrderAddressResult.getBaseAddress().getCityUk(), orderAddress.getBaseAddress().getCityUk());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getStreetUk(), orderAddress.getBaseAddress().getStreetUk());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getDistrictUk(),
            orderAddress.getBaseAddress().getDistrictUk());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getEntranceNumber(),
            orderAddress.getBaseAddress().getEntranceNumber());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getHouseNumber(),
            orderAddress.getBaseAddress().getHouseNumber());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getHouseCorpus(),
            orderAddress.getBaseAddress().getHouseCorpus());
        assertEquals(expectedOrderAddressResult.getCoordinates(), orderAddress.getCoordinates());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getActual(), orderAddress.getBaseAddress().getActual());
        assertEquals(expectedOrderAddressResult.getBaseAddress().getAddressStatus(),
            orderAddress.getBaseAddress().getAddressStatus());
    }

}
