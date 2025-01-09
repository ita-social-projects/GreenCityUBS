package greencity.service;

import org.springframework.stereotype.Service;

@Service
public interface PhoneNumberFormatterService {
    /**
     * Method gets E164 phone number format in UA region.
     *
     * @param phoneNumberStr {@link String}
     * @return {@link String} formatted phone number. Example: +380XXXXXXXXX.
     * @author Mykola Danylko
     */
    String getE164PhoneNumberFormat(String phoneNumberStr);
}
