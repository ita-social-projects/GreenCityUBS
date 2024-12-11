package greencity.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import greencity.annotations.ValidPhoneNumber;
import greencity.constant.ErrorMessage;
import greencity.exceptions.NotFoundException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidation implements ConstraintValidator<ValidPhoneNumber, String> {
    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // Initializes the validator in preparation for #isValid calls
    }

    /**
     * Method checks if phone number valid in UA region. Example of valid phone
     * numbers: +380XXXXXXXXX; 380XXXXXXXXX; 0XXXXXXXXX; XXXXXXXXX.
     *
     * @param value   {@link String} phone number.
     * @param context {@link ConstraintValidatorContext}
     * @return {@link Boolean}
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(value, "UA");
            return phoneUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException e) {
            throw new NotFoundException(ErrorMessage.PHONE_NUMBER_PARSING_FAIL + value);
        }
    }
}
