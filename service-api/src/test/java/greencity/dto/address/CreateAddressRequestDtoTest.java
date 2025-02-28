package greencity.dto.address;

import greencity.ModelUtils;
import greencity.dto.CreateAddressRequestDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateAddressRequestDtoTest {
    @Test
    void testAreAddressesEqualByCityAndRegionAndDistrict() {

        CreateAddressRequestDto address1 = ModelUtils.getAddressRequestDto1();
        CreateAddressRequestDto address2 = ModelUtils.getAddressRequestDto2();
        CreateAddressRequestDto address3 = ModelUtils.getAddressRequestDto3();
        CreateAddressRequestDto address4 = ModelUtils.getAddressRequestDto4();
        CreateAddressRequestDto address5 = ModelUtils.getAddressRequestDto5();

        boolean result1 = address1.areAddressesEqual(address2);
        assertTrue(result1);

        boolean result2 = address1.areAddressesEqual(address3);
        assertFalse(result2);

        boolean result3 = address1.areAddressesEqual(null);
        assertFalse(result3);

        boolean result5 = address1.areAddressesEqual(address4);
        assertFalse(result5);

        boolean result6 = address1.areAddressesEqual(address5);
        assertTrue(result6);
    }
}
