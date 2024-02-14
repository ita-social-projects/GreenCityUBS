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
        expectedAddress.setAddressStatus(AddressStatus.DELETED);

        Address actualAddress = addressDtoMapper.convert(addressDto);

        assertEquals(expectedAddress.getId(), actualAddress.getId());
        assertEquals(expectedAddress.getCity(), actualAddress.getCity());
        assertEquals(expectedAddress.getStreet(), actualAddress.getStreet());
        assertEquals(expectedAddress.getDistrict(), actualAddress.getDistrict());
        assertEquals(expectedAddress.getEntranceNumber(), actualAddress.getEntranceNumber());
        assertEquals(expectedAddress.getHouseNumber(), actualAddress.getHouseNumber());
        assertEquals(expectedAddress.getHouseCorpus(), actualAddress.getHouseCorpus());
        assertEquals(expectedAddress.getCoordinates(), actualAddress.getCoordinates());
        assertEquals(expectedAddress.getActual(), actualAddress.getActual());
        assertNotEquals(expectedAddress.getAddressStatus(), actualAddress.getAddressStatus());
    }

}
