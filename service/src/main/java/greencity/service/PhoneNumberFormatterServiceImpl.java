package greencity.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import greencity.constant.ErrorMessage;
import greencity.exceptions.EmployeeValidationException;
import org.springframework.stereotype.Service;

@Service
public class PhoneNumberFormatterServiceImpl implements PhoneNumberFormatterService {
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    /**
     * {@inheritDoc}
     */
    public String getE164PhoneNumberFormat(String phoneNumberStr) {
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phoneNumberStr, "UA");
            return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new EmployeeValidationException(ErrorMessage.PHONE_NUMBER_PARSING_FAIL + phoneNumberStr);
        }
    }
}
