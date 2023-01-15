package greencity.exceptions.tariff;

import greencity.constant.ErrorMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TariffAlreadyExistsExceptionTest {

    @Test
    void tariffAlreadyExistsExceptionMessageTest() {
        String message = "Tariff for such locations is already exists";
        TariffAlreadyExistsException t = new TariffAlreadyExistsException(message);
        Assertions.assertEquals(ErrorMessage.TARIFF_IS_ALREADY_EXISTS, t.getMessage());
    }
}
