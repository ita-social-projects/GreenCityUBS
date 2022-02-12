package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.AddressDto;
import greencity.entity.user.ubs.Address;

import java.util.Objects;

import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class AddressDtoMapperTest {

    @InjectMocks
    private AddressDtoMapper addressDtoMapper;

    @Test
    void convert() {
        AddressDto addressDto = ModelUtils.addressDto();
        Address expectedAddress = ModelUtils.address();
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
        assertFalse(Objects.equals(expectedAddress.getAddressStatus(), actualAddress.getAddressStatus()));
    }

}
