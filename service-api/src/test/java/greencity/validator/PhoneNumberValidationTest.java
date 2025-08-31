package greencity.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PhoneNumberValidationTest {
    private PhoneNumberValidation validator;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidation();
    }

    @Nested
    @DisplayName("Valid phone numbers")
    class ValidPhoneNumbers {

        @Test
        void shouldAcceptInternationalFormatWithPlus() {
            assertTrue(validator.isValid("+380938754569", null));
        }

        @Test
        void shouldAcceptFormattedInternationalWithSymbols() {
            assertTrue(validator.isValid("+38(093)87-54-569", null));
        }

        @Test
        void shouldAcceptInternationalFormatWithoutPlus() {
            assertTrue(validator.isValid("380998754569", null));
        }

        @Test
        void shouldAcceptNationalFormatStartingWithZero() {
            assertTrue(validator.isValid("0678754569", null));
        }

        @Test
        void shouldAcceptShortNationalFormatWithoutZero() {
            assertTrue(validator.isValid("938754569", null));
        }

        @Test
        void shouldAcceptValidPhoneNumberWithSpacesAround() {
            assertTrue(validator.isValid("  +380938754569  ", null));
        }
    }

    @Nested
    @DisplayName("Invalid phone numbers")
    class InvalidPhoneNumbers {

        @Test
        void shouldRejectInvalidCountryCode() {
            assertFalse(validator.isValid("0114860406", null));
        }

        @Test
        void shouldRejectTooShortNumber() {
            assertFalse(validator.isValid("4860406", null));
        }

        @Test
        void shouldRejectNumberWithLetters() {
            assertFalse(validator.isValid("067875Dhgjh4569", null));
        }

        @Test
        void shouldRejectCompletelyNonNumericString() {
            assertFalse(validator.isValid("jldjfdavn", null));
        }

        @Test
        void shouldRejectBlankString() {
            assertFalse(validator.isValid("   ", null));
        }

        @Test
        void shouldRejectEmptyString() {
            assertFalse(validator.isValid("", null));
        }
    }

    @Test
    @DisplayName("Should accept null as valid (field is optional)")
    void shouldAcceptNullValue() {
        assertTrue(validator.isValid(null, null));
    }
}