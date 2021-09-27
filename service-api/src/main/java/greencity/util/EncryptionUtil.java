package greencity.util;

import com.liqpay.LiqPayUtil;
import greencity.dto.PaymentRequestDto;
import greencity.dto.PaymentRequestDtoLiqPay;
import greencity.dto.PaymentResponseDto;
import greencity.dto.PaymentResponseDtoLiqPay;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

@Component
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
        if (dto.getFee() == null) {
            dto.setFee(0);
        }
        StringBuilder stringBuilder = new StringBuilder(password);
        checkInteger(dto.getActualAmount(), stringBuilder);
        checkString(dto.getActualCurrency(), stringBuilder);
        if (dto.getAdditionalInfo() != null) {
            checkString(dto.getAdditionalInfo().getBankName(), stringBuilder);
            checkString(dto.getAdditionalInfo().getBankCountry(), stringBuilder);
            checkString(dto.getAdditionalInfo().getBankResponseCode(), stringBuilder);
            checkString(dto.getAdditionalInfo().getCardProduct(), stringBuilder);
            checkString(dto.getAdditionalInfo().getCardCategory(), stringBuilder);
            checkDouble(dto.getAdditionalInfo().getSettlementFee(), stringBuilder);
            checkString(dto.getAdditionalInfo().getCaptureStatus(), stringBuilder);
            checkDouble(dto.getAdditionalInfo().getClientFee(), stringBuilder);
            checkString(dto.getAdditionalInfo().getIpaddressV4(), stringBuilder);
            checkDouble(dto.getAdditionalInfo().getCaptureAmount(), stringBuilder);
            checkString(dto.getAdditionalInfo().getCardType(), stringBuilder);
            checkString(dto.getAdditionalInfo().getReservationData(), stringBuilder);
            checkString(dto.getAdditionalInfo().getBankResponseDescription(), stringBuilder);
            checkInteger(dto.getAdditionalInfo().getTransactionId(), stringBuilder);
            checkString(dto.getAdditionalInfo().getTimeEnd(), stringBuilder);
            checkString(dto.getAdditionalInfo().getCardNumber(), stringBuilder);
        }
        checkInteger(dto.getAmount(), stringBuilder);
        checkString(dto.getApprovalCode(), stringBuilder);
        checkInteger(dto.getCardBin(), stringBuilder);
        checkString(dto.getCardType(), stringBuilder);
        checkString(dto.getCurrency(), stringBuilder);
        checkInteger(dto.getEci(), stringBuilder);
        checkInteger(dto.getFee(), stringBuilder);
        checkString(dto.getMaskedCard(), stringBuilder);
        checkString(dto.getMerchantData(), stringBuilder);
        checkInteger(dto.getMerchantId(), stringBuilder);
        checkString(dto.getOrderId(), stringBuilder);
        checkString(dto.getOrderStatus(), stringBuilder);
        checkString(dto.getOrderTime(), stringBuilder);
        checkInteger(dto.getPaymentId(), stringBuilder);
        checkString(dto.getPaymentSystem(), stringBuilder);
        checkString(dto.getProductId(), stringBuilder);
        checkString(dto.getRectoken(), stringBuilder);
        checkString(dto.getRectokenLifetime(), stringBuilder);
        checkInteger(dto.getResponseCode(), stringBuilder);
        checkString(dto.getResponseDescription(), stringBuilder);
        checkString(dto.getResponseStatus(), stringBuilder);
        checkIntegerInclude0(dto.getReversalAmount(), stringBuilder);
        checkString(dto.getRrn(), stringBuilder);
        checkString(dto.getSenderAccount(), stringBuilder);
        checkString(dto.getSenderCellPhone(), stringBuilder);
        checkString(dto.getSenderEmail(), stringBuilder);
        checkIntegerInclude0(dto.getSettlementAmount(), stringBuilder);
        checkString(dto.getSettlementCurrency(), stringBuilder);
        checkString(dto.getSettlementDate(), stringBuilder);
        checkString(dto.getTranType(), stringBuilder);
        checkString(dto.getVerificationStatus(), stringBuilder);
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

    private static void checkDouble(Double number, StringBuilder stringBuilder) {
        if (number != null && number != 0d) {
            stringBuilder.append("|" + number);
        }
    }

    private static void checkIntegerInclude0(Integer number, StringBuilder stringBuilder) {
        if (number != null) {
            stringBuilder.append("|" + number);
        }
    }

    /**
     * Method forms encrypted signature based on order details.
     * 
     * @param dto        {@link PaymentRequestDtoLiqPay}
     * @param privateKey - key from liqpay personal kabinet
     * @return {@link String} - encrypted signature;
     */
    public String formingRequestSignatureLiqPay(PaymentRequestDtoLiqPay dto, String privateKey) {
        JSONObject data = new JSONObject();
        data.put("public_key", dto.getPublicKey());
        data.put("version", dto.getVersion());
        data.put("action", dto.getAction());
        data.put("amount", dto.getAmount());
        data.put("currency", dto.getCurrency());
        data.put("description", dto.getDescription());
        data.put("order_id", dto.getOrderId());
        data.put("language", dto.getLanguage());
        data.put("paytypes", dto.getPaytypes());
        data.put("result_url", dto.getResultUrl());

        String dataToBase64 = Base64.encodeBase64String(data.toString().getBytes(StandardCharsets.UTF_8));

        return strToSignLiqPay(privateKey + dataToBase64 + privateKey);
    }

    /**
     * Method forms encrypted signature based on response.
     * 
     * @param gettedData {@link String} gettedDataFromLiqPay
     * @param privateKey - key from liqpay personal kabinet
     * @return {@link String} - ecnrypted signature;
     */
    public String formingResponseSignatureLiqPay(PaymentResponseDtoLiqPay dto, String gettedData, String privateKey) {
        return strToSignLiqPay(privateKey + gettedData + privateKey);
    }

    private String strToSignLiqPay(String str) {
        return LiqPayUtil.base64_encode(LiqPayUtil.sha1(str));
    }
}
