package greencity.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import greencity.constant.ErrorMessage;
import greencity.exceptions.PhoneNumberParseException;
import org.springframework.stereotype.Service;

@Service
public class UAPhoneNumberUtil {
    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * {@inheritDoc}
     */
    public static String getE164PhoneNumberFormat(String phoneNumberStr) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberStr, "UA");
            return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new PhoneNumberParseException(ErrorMessage.PHONE_NUMBER_PARSING_FAIL + phoneNumberStr);
        }
    }
}
