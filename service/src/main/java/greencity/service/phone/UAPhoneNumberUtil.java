package greencity.service.phone;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import greencity.constant.ErrorMessage;
import greencity.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UAPhoneNumberUtil {
    private UAPhoneNumberUtil() {
    }

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * Method gets E164 phone number format in UA region.
     *
     * @param phoneNumberStr {@link String}
     * @return {@link String} formatted phone number. Example: +380XXXXXXXXX.
     * @author Mykola Danylko
     */

    public static String getE164PhoneNumberFormat(String phoneNumberStr) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberStr, "UA");
            return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new NotFoundException(ErrorMessage.PHONE_NUMBER_PARSING_FAIL + phoneNumberStr);
        }
    }
}
