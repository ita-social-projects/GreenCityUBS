package greencity.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintValidatorContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PhoneNumberValidationTest {

    @Mock
    private ConstraintValidatorContext context;
    private PhoneNumberValidation validation = new PhoneNumberValidation();

    @Test
    void isValid() {
        String internationalFormat = "+380938754569";
        String internationalFormat1 = "+38(093)87-54-569";
        String internationalFormatWithoutPlus = "380998754569";
        String nationalFormat = "0678754569";
        String nationalFormatWithoutZero = "938754569";

        String incorrectFormat1 = "0114860406";
        String incorrectFormat2 = "4860406";
        String incorrectFormat3 = "067875Dhgjh4569";

        assertTrue(validation.isValid(internationalFormat, context));
        assertTrue(validation.isValid(internationalFormat1, context));
        assertTrue(validation.isValid(internationalFormatWithoutPlus, context));
        assertTrue(validation.isValid(nationalFormat, context));
        assertTrue(validation.isValid(nationalFormatWithoutZero, context));

        assertFalse(validation.isValid(incorrectFormat1, context));
        assertFalse(validation.isValid(incorrectFormat2, context));
        assertFalse(validation.isValid(incorrectFormat3, context));
    }

    @Test
    void isValidShouldReturnFalseForInvalidPhoneNumber() {
        String incorrectStr = "jldjfdavn";
        boolean isValid = validation.isValid(incorrectStr, context);
        assertFalse(isValid, "Expected isValid() to return false for an invalid phone number");
    }

}