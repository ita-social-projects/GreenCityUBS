package greencity.service.phone;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberFormatterServiceImplTest {
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Test
    void getE164PhoneNumberFormat() throws NumberParseException {
        String internationalFormat = "+380938754569";
        String internationalFormat1 = "+38(093)87-54-569";
        String internationalFormatWithoutPlus = "380938754569";
        String nationalFormat = "0938754569";
        String nationalFormatWithoutZero = "938754569";

        String result = "+380938754569";

        Phonenumber.PhoneNumber phoneNumber1 = phoneNumberUtil.parse(internationalFormat, "UA");
        Phonenumber.PhoneNumber phoneNumber2 = phoneNumberUtil.parse(internationalFormat1, "UA");
        Phonenumber.PhoneNumber phoneNumber3 = phoneNumberUtil.parse(internationalFormatWithoutPlus, "UA");
        Phonenumber.PhoneNumber phoneNumber4 = phoneNumberUtil.parse(nationalFormat, "UA");
        Phonenumber.PhoneNumber phoneNumber5 = phoneNumberUtil.parse(nationalFormatWithoutZero, "UA");

        assertEquals(result, phoneNumberUtil.format(phoneNumber1, PhoneNumberUtil.PhoneNumberFormat.E164));
        assertEquals(result, phoneNumberUtil.format(phoneNumber2, PhoneNumberUtil.PhoneNumberFormat.E164));
        assertEquals(result, phoneNumberUtil.format(phoneNumber3, PhoneNumberUtil.PhoneNumberFormat.E164));
        assertEquals(result, phoneNumberUtil.format(phoneNumber4, PhoneNumberUtil.PhoneNumberFormat.E164));
        assertEquals(result, phoneNumberUtil.format(phoneNumber5, PhoneNumberUtil.PhoneNumberFormat.E164));
    }

    @Test
    void getE164PhoneNumberFormatNumberParseException() {
        Exception thrown = assertThrows(NumberParseException.class,
            () -> phoneNumberUtil.parse("dfadfadf", "UA"));
        assertNotNull(thrown);
    }
}