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
     * @param dto        {@link PaymentResponseDtoLiqPay}
     * @param privateKey - key from liqpay personal kabinet
     * @return {@link String} - ecnrypted signature;
     */
    public String formingResponseSignatureLiqPay(PaymentResponseDtoLiqPay dto, String privateKey) {
        JSONObject data = new JSONObject();
        data.put("acq_id", dto.getAcqId());
        data.put("action", dto.getAction());
        data.put("agent_commission", dto.getAgentCommission());
        data.put("amount", dto.getAmount());
        data.put("amount_bonus", dto.getAmountBonus());
        data.put("amount_credit", dto.getAmountCredit());
        data.put("amount_debit", dto.getAmountDebit());
        data.put("bonus_procent", dto.getBonusProcent());
        data.put("bonus_type", dto.getBonusType());
        data.put("card_token", dto.getCardToken());
        data.put("commission_credit", dto.getCommissionCredit());
        data.put("commission_debit", dto.getCommissionDebit());
        data.put("completion_date", dto.getCompletionDate());
        data.put("create_date", dto.getCreateDate());
        data.put("currency", dto.getCurrency());
        data.put("currency_credit", dto.getCurrencyCredit());
        data.put("currency_debit", dto.getCurrencyDebit());
        data.put("customer", dto.getCustomer());
        data.put("description", dto.getDescription());
        data.put("end_date", dto.getEndDate());
        data.put("err_code", dto.getErrCode());
        data.put("err_description", dto.getErrDescription());
        data.put("info", dto.getInfo());
        data.put("ip", dto.getIp());
        data.put("is_3ds", dto.getIs3Ds());
        data.put("liqpay_order_id", dto.getLiqpayOrderId());
        data.put("mpi_eci", dto.getMpiEci());
        data.put("order_id", dto.getOrderId());
        data.put("payment_id", dto.getPaymentId());
        data.put("paytype", dto.getPaytype());
        data.put("public_key", dto.getPublicKey());
        data.put("receiver_commission", dto.getReceiverCommission());
        data.put("redirect_to", dto.getRedirectTo());
        data.put("refund_date_last", dto.getRefundDateLast());
        data.put("rrn_credit", dto.getRrnCredit());
        data.put("rrn_debit", dto.getRrnDebit());
        data.put("sender_bonus", dto.getSenderBonus());
        data.put("sender_card_bank", dto.getSenderCardBank());
        data.put("sender_card_country", dto.getSenderCardCountry());
        data.put("sender_card_mask2", dto.getSenderCardMask2());
        data.put("sender_card_type", dto.getSenderCardType());
        data.put("sender_commission", dto.getSenderCommission());
        data.put("sender_first_name", dto.getSenderFirstName());
        data.put("sender_last_name", dto.getSenderLastName());
        data.put("sender_phone", dto.getSenderPhone());
        data.put("status", dto.getStatus());
        data.put("token", dto.getToken());
        data.put("type", dto.getType());
        data.put("version", dto.getVersion());
        data.put("err_erc", dto.getErrErc());
        data.put("product_category", dto.getProductCategory());
        data.put("product_description", dto.getProductDescription());
        data.put("product_name", dto.getProductName());
        data.put("product_url", dto.getProductUrl());
        data.put("refund_amount", dto.getRefundAmount());
        data.put("verifycode", dto.getVerifycode());

        String dataCoddedByBase64 = Base64.encodeBase64String(data.toString().getBytes(StandardCharsets.UTF_8));

        return strToSignLiqPay(privateKey + dataCoddedByBase64 + privateKey);
    }

    private String strToSignLiqPay(String str) {
        return LiqPayUtil.base64_encode(LiqPayUtil.sha1(str));
    }
}
