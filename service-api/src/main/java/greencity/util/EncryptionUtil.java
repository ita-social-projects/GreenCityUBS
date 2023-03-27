package greencity.util;

import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

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
        StringBuilder stringBuilder = new StringBuilder(password);
        stringBuilder.append("|" + dto.getAmount());
        stringBuilder.append("|" + dto.getCurrency());
        stringBuilder.append("|" + merchantId);
        stringBuilder.append("|" + dto.getOrderDescription());
        stringBuilder.append("|" + dto.getOrderId());
        stringBuilder.append("|" + dto.getResponseUrl());
        return sha1Hex(stringBuilder.toString());
    }

    /**
     * Method checks if response details is valid.
     *
     * @param dto      {@link PaymentResponseDto} - response order data.
     * @param password - fondy password.
     * @return {@link Boolean} - whether the data is valid.
     */
    public boolean checkIfResponseSignatureIsValid(PaymentResponseDto dto, String password) {
        StringBuilder stringBuilder = new StringBuilder(password);
        checkInteger(dto.getActual_amount(), stringBuilder);
        checkString(dto.getActual_currency(), stringBuilder);
        checkInteger(dto.getAmount(), stringBuilder);
        checkString(dto.getApproval_code(), stringBuilder);
        checkInteger(dto.getCard_bin(), stringBuilder);
        checkString(dto.getCard_type(), stringBuilder);
        checkString(dto.getCurrency(), stringBuilder);
        checkInteger(dto.getEci(), stringBuilder);
        checkInteger(dto.getFee(), stringBuilder);
        checkString(dto.getMasked_card(), stringBuilder);
        checkString(dto.getMerchant_data(), stringBuilder);
        checkInteger(dto.getMerchant_id(), stringBuilder);
        checkString(dto.getOrder_id(), stringBuilder);
        checkString(dto.getOrder_status(), stringBuilder);
        checkString(dto.getOrder_time(), stringBuilder);
        checkInteger(dto.getPayment_id(), stringBuilder);
        checkString(dto.getPayment_system(), stringBuilder);
        checkString(dto.getProduct_id(), stringBuilder);
        checkString(dto.getRectoken(), stringBuilder);
        checkString(dto.getRectoken_lifetime(), stringBuilder);
        checkInteger(dto.getResponse_code(), stringBuilder);
        checkString(dto.getResponse_description(), stringBuilder);
        checkString(dto.getResponse_status(), stringBuilder);
        checkIntegerInclude0(dto.getReversal_amount(), stringBuilder);
        checkString(dto.getRrn(), stringBuilder);
        checkString(dto.getSender_account(), stringBuilder);
        checkString(dto.getSender_cell_phone(), stringBuilder);
        checkString(dto.getSender_email(), stringBuilder);
        checkIntegerInclude0(dto.getSettlement_amount(), stringBuilder);
        checkString(dto.getSettlement_currency(), stringBuilder);
        checkString(dto.getSettlement_date(), stringBuilder);
        checkString(dto.getTran_type(), stringBuilder);
        checkString(dto.getVerification_status(), stringBuilder);
        checkInteger(dto.getParent_order_id(), stringBuilder);
        return DigestUtils.sha1Hex(stringBuilder.toString()).equals(dto.getSignature());
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

}
