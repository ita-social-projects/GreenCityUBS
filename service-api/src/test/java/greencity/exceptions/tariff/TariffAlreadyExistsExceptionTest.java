package greencity.exceptions.tariff;

import greencity.constant.ErrorMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TariffAlreadyExistsExceptionTest {

    @Test
    void givenValidDtoWhenValidatedThenNoValidationError() {
        try {
            new TariffAlreadyExistsException(ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
        } catch (TariffAlreadyExistsException e) {
            Assertions.assertEquals(e.getMessage(), ErrorMessage.TARIFF_IS_ALREADY_EXISTS);
        }
    }
}
