package greencity.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import greencity.annotations.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator class that checks if a phone number string is valid for the
 * Ukrainian region ("UA") using Google's libphonenumber library. The phone
 * number must be:
 * <ul>
 * <li>Non-blank</li>
 * <li>Parsable and valid according to libphonenumber rules</li>
 * </ul>
 * Examples of valid formats:
 * <ul>
 * <li>+380XXXXXXXXX</li>
 * <li>380XXXXXXXXX</li>
 * <li>0XXXXXXXXX</li>
 * </ul>
 */
public class PhoneNumberValidation implements ConstraintValidator<ValidPhoneNumber, String> {
    private static final String DEFAULT_REGION = "UA";
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * Validates a given phone number string.
     *
     * @param value   the phone number string to validate.
     * @param context the context in which the constraint is evaluated.
     * @return {@code true} if the phone number is valid, {@code false} otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String trimmedValue = value.trim();
        if (trimmedValue.isBlank()) {
            return false;
        }

        if (trimmedValue.length() != 13) {
            return false;
        }

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(trimmedValue, DEFAULT_REGION);
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            return false;
        }
    }
}
