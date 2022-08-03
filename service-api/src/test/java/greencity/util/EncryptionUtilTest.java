package greencity.util;

import com.liqpay.LiqPayUtil;
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
        PaymentResponseDto paymentResponseDto = PaymentResponseDto.builder()
                .actual_amount(10)
                .actual_currency("USD")
                .amount(2)
                .approval_code("123")
                .card_bin(444455)
                .card_type("VISA")
                .currency("USD")
                .eci(6)
                .fee(10)
                .masked_card("1")
                .merchant_data("03/08/22")
                .merchant_id(3)
                .order_id("234")
                .order_status("approved")
                .order_time("13/08/22")
                .payment_id(85233820)
                .payment_system("card")
                .product_id("324")
                .rectoken("Y")
                .rectoken_lifetime("256")
                .response_code(200)
                .response_description("")
                .response_status("APPROVED")
                .reversal_amount(2)
                .rrn("IADG/0000000C00000000")
                .sender_account("sender")
                .sender_cell_phone("+4930901820")
                .sender_email("sender@mail.com")
                .settlement_currency("USD")
                .settlement_date("03/08/22")
                .tran_type("purchase")
                .verification_status("ACTIVE")
                .parent_order_id(1)
                .build();

        paymentResponseDto.setSignature(SIGNATURE);

        Assert.assertEquals(Boolean.TRUE
                , encryptionUtil.checkIfResponseSignatureIsValid(paymentResponseDto, PASSWORD));
        Assert.assertEquals(Boolean.FALSE
                , encryptionUtil.checkIfResponseSignatureIsValid(paymentResponseDto, INVALID_PASSWORD));
    }

    @Test
    public void formRequestSignature() {
        PaymentRequestDto paymentRequestDto = PaymentRequestDto.builder()
                .orderId("1")
                .merchantId(2)
                .orderDescription("")
                .currency("USD")
                .amount(2)
                .signature("")
                .responseUrl("responseUrl")
                .build();

        String stringBuilder = PASSWORD + "|" + paymentRequestDto.getAmount() +
                "|" + paymentRequestDto.getCurrency() +
                "|" + MERCHANT_ID +
                "|" + paymentRequestDto.getOrderDescription() +
                "|" + paymentRequestDto.getOrderId() +
                "|" + paymentRequestDto.getResponseUrl();
        String expected = sha1Hex(stringBuilder);

        Assert.assertEquals(expected
                , encryptionUtil.formRequestSignature(paymentRequestDto, PASSWORD, MERCHANT_ID));
    }

    @Test
    public void formingRequestSignatureLiqPay() {
        PaymentRequestDtoLiqPay paymentRequestDtoLiqPay = PaymentRequestDtoLiqPay.builder()
                .publicKey("publicKey")
                .version(3)
                .action("pay")
                .amount(2)
                .currency("USD")
                .description("description")
                .orderId("233")
                .language("eng")
                .paytypes("card")
                .resultUrl("resultUrl")
                .build();

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

        Assert.assertEquals(expected
                , encryptionUtil.formingRequestSignatureLiqPay(paymentRequestDtoLiqPay, PRIVATE_KEY));
    }

    @Test
    public void formingResponseSignatureLiqPay() {

        String expected = LiqPayUtil.base64_encode(LiqPayUtil.sha1(PRIVATE_KEY + "data" + PRIVATE_KEY));

        Assert.assertEquals(expected
                , encryptionUtil.formingResponseSignatureLiqPay("data", PRIVATE_KEY));
    }
}
