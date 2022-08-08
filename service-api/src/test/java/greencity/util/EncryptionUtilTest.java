package greencity.util;

import com.liqpay.LiqPayUtil;
import greencity.ModelUtils;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentRequestDtoLiqPay;
import greencity.dto.payment.PaymentResponseDto;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;

import static greencity.ModelUtils.getPaymentRequestDto;
import static greencity.ModelUtils.getPaymentRequestDtoLiqPay;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;

@ExtendWith(MockitoExtension.class)
public class EncryptionUtilTest {

    @InjectMocks
    EncryptionUtil encryptionUtil = new EncryptionUtil();
    private static final String PASSWORD = "One2three";
    private static final String INVALID_PASSWORD = "password";
    private static final String SIGNATURE = "c972ca8f1eb227d85631728d690037cfa41375af";
    private static final String MERCHANT_ID = "3";
    private static final String PRIVATE_KEY = "privateKey";

    @Test
    public void checkIfResponseSignatureIsValid() {
        PaymentResponseDto paymentResponseDto = ModelUtils.getPaymentResponseDto();

        paymentResponseDto.setSignature(SIGNATURE);

        Assert.assertEquals(Boolean.TRUE, encryptionUtil.checkIfResponseSignatureIsValid(paymentResponseDto, PASSWORD));
        Assert.assertEquals(Boolean.FALSE,
            encryptionUtil.checkIfResponseSignatureIsValid(paymentResponseDto, INVALID_PASSWORD));
    }

    @Test
    public void formRequestSignature() {
        PaymentRequestDto paymentRequestDto = getPaymentRequestDto();

        String stringBuilder = PASSWORD + "|" + paymentRequestDto.getAmount() +
            "|" + paymentRequestDto.getCurrency() +
            "|" + MERCHANT_ID +
            "|" + paymentRequestDto.getOrderDescription() +
            "|" + paymentRequestDto.getOrderId() +
            "|" + paymentRequestDto.getResponseUrl();
        String expected = sha1Hex(stringBuilder);

        Assert.assertEquals(expected, encryptionUtil.formRequestSignature(paymentRequestDto, PASSWORD, MERCHANT_ID));
    }

    @Test
    public void formingRequestSignatureLiqPay() {
        PaymentRequestDtoLiqPay paymentRequestDtoLiqPay = getPaymentRequestDtoLiqPay();

        JSONObject data = new JSONObject();
        data.put("public_key", paymentRequestDtoLiqPay.getPublicKey());
        data.put("version", paymentRequestDtoLiqPay.getVersion());
        data.put("action", paymentRequestDtoLiqPay.getAction());
        data.put("amount", paymentRequestDtoLiqPay.getAmount());
        data.put("currency", paymentRequestDtoLiqPay.getCurrency());
        data.put("description", paymentRequestDtoLiqPay.getDescription());
        data.put("order_id", paymentRequestDtoLiqPay.getOrderId());
        data.put("language", paymentRequestDtoLiqPay.getLanguage());
        data.put("paytypes", paymentRequestDtoLiqPay.getPaytypes());
        data.put("result_url", paymentRequestDtoLiqPay.getResultUrl());

        String dataToBase64 = Base64.encodeBase64String(data.toString().getBytes(StandardCharsets.UTF_8));

        String expected = LiqPayUtil.base64_encode(LiqPayUtil.sha1(PRIVATE_KEY + dataToBase64 + PRIVATE_KEY));

        Assert.assertEquals(expected,
            encryptionUtil.formingRequestSignatureLiqPay(paymentRequestDtoLiqPay, PRIVATE_KEY));
    }

    @Test
    public void formingResponseSignatureLiqPay() {

        String expected = LiqPayUtil.base64_encode(LiqPayUtil.sha1(PRIVATE_KEY + "data" + PRIVATE_KEY));

        Assert.assertEquals(expected, encryptionUtil.formingResponseSignatureLiqPay("data", PRIVATE_KEY));
    }
}
