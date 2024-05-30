package greencity.util;

import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

@Component
@ToString
public class EncryptionUtil {
    /**
     * Method forms encrypted signature based on order details.
     *
     * @param dto        {@link PaymentRequestDto} - request order data.
     * @param password   - fondy password.
     * @param merchantId - fondy merchant id.
     * @return {@String} - encrypted signature.
     */
    public String formRequestSignature(PaymentRequestDto dto, String password, String merchantId) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(dto.getMerchantAccount()).append(";");
        stringBuilder.append(dto.getMerchantDomainName()).append(";");
        stringBuilder.append(dto.getOrderReference()).append(";");
        stringBuilder.append(dto.getOrderDate()).append(";");
        stringBuilder.append(dto.getAmount()).append(";");
        stringBuilder.append(dto.getCurrency()).append(";");
        for (String name : dto.getProductName()) {
            stringBuilder.append(name).append(";");
        }
        for (Integer count : dto.getProductCount()) {
            stringBuilder.append(count).append(";");
        }
        for (Integer price : dto.getProductPrice()) {
            stringBuilder.append(price).append(";");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String hmacMD5 = new HmacUtils("HmacMD5", password).hmacHex(stringBuilder.toString());
        return hmacMD5;
    }

    /**
     * Forms an encrypted signature based on the response from WayForPay. This
     * method uses the HMAC-MD5 algorithm to create a hash-based message
     * authentication code (HMAC). The HMAC is created from a string that is built
     * by appending the order reference, status, and time from the response DTO,
     * each separated by a semicolon. The HMAC is then returned as a hexadecimal
     * string.
     *
     * @param dto      {@link PaymentResponseWayForPay} - response data from
     *                 WayForPay.
     * @param password The password used for the HMAC-MD5 encryption.
     * @return {@String} - The HMAC-MD5 encrypted signature.
     */
    public String formResponseSignature(PaymentResponseWayForPay dto, String password) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dto.getOrderReference()).append(";");
        stringBuilder.append(dto.getStatus()).append(";");
        stringBuilder.append(dto.getTime());
        String hmacMD5 = new HmacUtils("HmacMD5", password).hmacHex(stringBuilder.toString());
        return hmacMD5;
    }

    private static void checkString(String string, StringBuilder stringBuilder) {
        if (string != null && !string.equals("")) {
            stringBuilder.append("|" + string);
        }
    }

    private static void checkInteger(Integer number, StringBuilder stringBuilder) {
        if (number != null && number != 0) {
            stringBuilder.append("|" + number);
        }
    }

    private static void checkIntegerInclude0(Integer number, StringBuilder stringBuilder) {
        if (number != null) {
            stringBuilder.append("|" + number);
        }
    }

    private Double convertCoinsIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(2)
            .setScale(0, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
