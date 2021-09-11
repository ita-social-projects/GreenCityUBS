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
        checkInteger(dto.getActual_amount(), stringBuilder);
        checkString(dto.getActual_currency(), stringBuilder);
        if (dto.getAdditional_info() != null) {
            checkString(dto.getAdditional_info().getBank_name(), stringBuilder);
            checkString(dto.getAdditional_info().getBank_country(), stringBuilder);
            checkString(dto.getAdditional_info().getBank_response_code(), stringBuilder);
            checkString(dto.getAdditional_info().getCard_product(), stringBuilder);
            checkString(dto.getAdditional_info().getCard_category(), stringBuilder);
            checkDouble(dto.getAdditional_info().getSettlement_fee(), stringBuilder);
            checkString(dto.getAdditional_info().getCapture_status(), stringBuilder);
            checkDouble(dto.getAdditional_info().getClient_fee(), stringBuilder);
            checkString(dto.getAdditional_info().getIpaddress_v4(), stringBuilder);
            checkDouble(dto.getAdditional_info().getCapture_amount(), stringBuilder);
            checkString(dto.getAdditional_info().getCard_type(), stringBuilder);
            checkString(dto.getAdditional_info().getReservation_data(), stringBuilder);
            checkString(dto.getAdditional_info().getBank_response_description(), stringBuilder);
            checkInteger(dto.getAdditional_info().getTransaction_id(), stringBuilder);
            checkString(dto.getAdditional_info().getTimeend(), stringBuilder);
            checkString(dto.getAdditional_info().getCard_number(), stringBuilder);
        }
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
