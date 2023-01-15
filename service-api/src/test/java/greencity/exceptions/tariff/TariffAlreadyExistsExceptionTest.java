package greencity.exceptions.tariff;

import greencity.constant.ErrorMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TariffAlreadyExistsExceptionTest {

    @Test
    void givenValidDtoWhenValidatedThenNoValidationError() {
        String message = "Tariff for such locations is already exists";
        TariffAlreadyExistsException t = new TariffAlreadyExistsException(message);
        Assertions.assertEquals(t.getMessage(), ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
    }
}
