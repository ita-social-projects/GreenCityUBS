package greencity.mapping.user;

import greencity.ModelUtils;
import greencity.dto.address.AddressDto;
import greencity.entity.user.ubs.Address;
import greencity.enums.AddressStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
class AddressDtoMapperTest {

    @InjectMocks
    private AddressDtoMapper addressDtoMapper;

    @Test
    void convert() {
        AddressDto addressDto = ModelUtils.addressDto();

        Address expectedAddress = ModelUtils.getAddress();
        expectedAddress.getBaseAddress().setAddressStatus(AddressStatus.DELETED);

        Address actualAddress = addressDtoMapper.convert(addressDto);

        assertEquals(expectedAddress.getId(), actualAddress.getId());
        assertEquals(expectedAddress.getBaseAddress().getCityUk(), actualAddress.getBaseAddress().getCityUk());
        assertEquals(expectedAddress.getBaseAddress().getStreetUk(), actualAddress.getBaseAddress().getStreetUk());
        assertEquals(expectedAddress.getBaseAddress().getDistrictUk(), actualAddress.getBaseAddress().getDistrictUk());
        assertEquals(expectedAddress.getBaseAddress().getEntranceNumber(),
            actualAddress.getBaseAddress().getEntranceNumber());
        assertEquals(expectedAddress.getBaseAddress().getHouseNumber(),
            actualAddress.getBaseAddress().getHouseNumber());
        assertEquals(expectedAddress.getBaseAddress().getHouseCorpus(),
            actualAddress.getBaseAddress().getHouseCorpus());
        assertEquals(expectedAddress.getCoordinates(), actualAddress.getCoordinates());
        assertEquals(expectedAddress.getBaseAddress().getActual(), actualAddress.getBaseAddress().getActual());
        assertNotEquals(expectedAddress.getBaseAddress().getAddressStatus(),
            actualAddress.getBaseAddress().getAddressStatus());
    }

}
