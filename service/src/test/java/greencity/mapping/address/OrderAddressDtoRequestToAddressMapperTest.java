package greencity.mapping.address;

import greencity.ModelUtils;
import greencity.dto.order.OrderAddressDtoRequest;
import greencity.entity.user.ubs.Address;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderAddressDtoRequestToAddressMapperTest {
    private static final OrderAddressDtoRequestToAddressMapper mapper = new OrderAddressDtoRequestToAddressMapper();

    @Test
    void shouldConvertOrderAddressDtoRequestToAddressProperlyTest() {
        OrderAddressDtoRequest orderAddressDtoRequest = ModelUtils.getTestOrderAddressDtoRequest();
        Address address = mapper.convert(orderAddressDtoRequest);
        assertNotNull(address);
        assertEquals(orderAddressDtoRequest.getId(), address.getId());
        assertEquals(orderAddressDtoRequest.getCoordinates().getLatitude(), address.getCoordinates().getLatitude());
        assertEquals(orderAddressDtoRequest.getCoordinates().getLongitude(), address.getCoordinates().getLongitude());
        assertEquals(orderAddressDtoRequest.getCityEn(), address.getBaseAddress().getCityEn());
        assertEquals(orderAddressDtoRequest.getCityUk(), address.getBaseAddress().getCityUk());
        assertEquals(orderAddressDtoRequest.getStreetEn(), address.getBaseAddress().getStreetEn());
        assertEquals(orderAddressDtoRequest.getStreetUk(), address.getBaseAddress().getStreetUk());
        assertEquals(orderAddressDtoRequest.getDistrictEn(), address.getBaseAddress().getDistrictEn());
        assertEquals(orderAddressDtoRequest.getDistrictUk(), address.getBaseAddress().getDistrictUk());
        assertEquals(orderAddressDtoRequest.getHouseNumber(), address.getBaseAddress().getHouseNumber());
        assertEquals(orderAddressDtoRequest.getHouseCorpus(), address.getBaseAddress().getHouseCorpus());
        assertEquals(orderAddressDtoRequest.getEntranceNumber(), address.getBaseAddress().getEntranceNumber());
    }
}
