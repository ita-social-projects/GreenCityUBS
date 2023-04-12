package greencity.util;

import greencity.ModelUtils;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getPaymentRequestDto;
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

}
