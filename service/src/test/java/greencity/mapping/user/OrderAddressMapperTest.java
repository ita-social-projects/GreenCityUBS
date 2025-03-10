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
        assertEquals(expectedOrderAddressResult.getAddress().getCityUk(), orderAddress.getAddress().getCityUk());
        assertEquals(expectedOrderAddressResult.getAddress().getStreetUk(), orderAddress.getAddress().getStreetUk());
        assertEquals(expectedOrderAddressResult.getAddress().getDistrictUk(),
            orderAddress.getAddress().getDistrictUk());
        assertEquals(expectedOrderAddressResult.getAddress().getEntranceNumber(),
            orderAddress.getAddress().getEntranceNumber());
        assertEquals(expectedOrderAddressResult.getAddress().getHouseNumber(),
            orderAddress.getAddress().getHouseNumber());
        assertEquals(expectedOrderAddressResult.getAddress().getHouseCorpus(),
            orderAddress.getAddress().getHouseCorpus());
        assertEquals(expectedOrderAddressResult.getCoordinates(), orderAddress.getCoordinates());
        assertEquals(expectedOrderAddressResult.getAddress().getActual(), orderAddress.getAddress().getActual());
        assertEquals(expectedOrderAddressResult.getAddress().getAddressStatus(),
            orderAddress.getAddress().getAddressStatus());
    }

}
