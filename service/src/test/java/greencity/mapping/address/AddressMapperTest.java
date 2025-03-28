package greencity.mapping.address;

import greencity.ModelUtils;
import greencity.dto.order.OrderAddressExportDetailsDtoUpdate;
import greencity.entity.user.ubs.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AddressMapperTest {
    @InjectMocks
    private AddressMapper addressMapper;

    @Test
    void mapOrderAddressExportDetailsDtoUpdateToAddressTest() {
        OrderAddressExportDetailsDtoUpdate dto = ModelUtils.getOrderAddressExportDetailsDtoUpdate();
        Address address;
        address = addressMapper.convert(dto);

        assertEquals(address.getId(), dto.getId());
        assertEquals(address.getBaseAddress().getAddressComment(), dto.getAddressComment());
        assertEquals(address.getBaseAddress().getRegionEn(), dto.getRegionEn());
        assertEquals(address.getBaseAddress().getRegionUk(), dto.getRegionUk());
        assertEquals(address.getBaseAddress().getDistrictEn(), dto.getDistrictEn());
        assertEquals(address.getBaseAddress().getDistrictUk(), dto.getDistrictUk());
        assertEquals(address.getBaseAddress().getCityEn(), dto.getCityEn());
        assertEquals(address.getBaseAddress().getCityUk(), dto.getCityUk());
        assertEquals(address.getBaseAddress().getStreetEn(), dto.getStreetEn());
        assertEquals(address.getBaseAddress().getStreetUk(), dto.getStreetUk());
        assertEquals(address.getBaseAddress().getHouseNumber(), dto.getHouseNumber());
        assertEquals(address.getBaseAddress().getHouseCorpus(), dto.getHouseCorpus());
        assertEquals(address.getBaseAddress().getEntranceNumber(), dto.getEntranceNumber());

    }
}
