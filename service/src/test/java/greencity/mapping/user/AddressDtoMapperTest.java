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
        expectedAddress.getAddress().setAddressStatus(AddressStatus.DELETED);

        Address actualAddress = addressDtoMapper.convert(addressDto);

        assertEquals(expectedAddress.getId(), actualAddress.getId());
        assertEquals(expectedAddress.getAddress().getCityUk(), actualAddress.getAddress().getCityUk());
        assertEquals(expectedAddress.getAddress().getStreetUk(), actualAddress.getAddress().getStreetUk());
        assertEquals(expectedAddress.getAddress().getDistrictUk(), actualAddress.getAddress().getDistrictUk());
        assertEquals(expectedAddress.getAddress().getEntranceNumber(), actualAddress.getAddress().getEntranceNumber());
        assertEquals(expectedAddress.getAddress().getHouseNumber(), actualAddress.getAddress().getHouseNumber());
        assertEquals(expectedAddress.getAddress().getHouseCorpus(), actualAddress.getAddress().getHouseCorpus());
        assertEquals(expectedAddress.getCoordinates(), actualAddress.getCoordinates());
        assertEquals(expectedAddress.getAddress().getActual(), actualAddress.getAddress().getActual());
        assertNotEquals(expectedAddress.getAddress().getAddressStatus(), actualAddress.getAddress().getAddressStatus());
    }

}
