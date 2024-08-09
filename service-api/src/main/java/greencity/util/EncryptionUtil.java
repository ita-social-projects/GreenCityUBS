package greencity.util;

import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import java.util.StringJoiner;
import lombok.ToString;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

@Component
@ToString
public class EncryptionUtil {
    /**
     * Method forms encrypted signature based on order details.
     *
     * @param dto      {@link PaymentRequestDto} - request order data.
     * @param password - way for pay password.
     * @return {@link String} - encrypted signature.
     */
    public String formRequestSignature(PaymentRequestDto dto, String password) {
        StringJoiner stringJoiner = new StringJoiner(";");
        stringJoiner.add(dto.getMerchantAccount())
            .add(dto.getMerchantDomainName())
            .add(dto.getOrderReference())
            .add(dto.getOrderDate().toString())
            .add(dto.getAmount().toString())
            .add(dto.getCurrency());
        dto.getProductName().forEach(stringJoiner::add);
        dto.getProductCount().forEach(count -> stringJoiner.add(count.toString()));
        dto.getProductPrice().forEach(price -> stringJoiner.add(price.toString()));

        return new HmacUtils("HmacMD5", password).hmacHex(stringJoiner.toString());
    }

    /**
     * Forms an encrypted signature based on the response from Way For Pay. This
     * method uses the HMAC-MD5 algorithm to create a hash-based message
     * authentication code (HMAC). The HMAC is created from a string that is built
     * by appending the order reference, status, and time from the response DTO,
     * each separated by a semicolon. The HMAC is then returned as a hexadecimal
     * string.
     *
     * @param dto      {@link PaymentResponseWayForPay} - response data from
     *                 WayForPay.
     * @param password The password used for the HMAC-MD5 encryption.
     * @return {@link String} - The HMAC-MD5 encrypted signature.
     */
    public String formResponseSignature(PaymentResponseWayForPay dto, String password) {
        StringJoiner stringJoiner = new StringJoiner(";");
        stringJoiner.add(dto.getOrderReference())
            .add((dto.getStatus()))
            .add(dto.getTime());
        return new HmacUtils("HmacMD5", password).hmacHex(stringJoiner.toString());
    }
}
