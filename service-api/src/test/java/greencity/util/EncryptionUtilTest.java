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
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@ExtendWith(MockitoExtension.class)
public class EncryptionUtilTest {

    @InjectMocks
    EncryptionUtil encryptionUtil = new EncryptionUtil();
    private static final String PASSWORD = "One2three";
    private static final String INVALID_PASSWORD = "password";
    private static final String SIGNATURE = "5aea51ef7b8b7a91ec4cc5ce8ccae59bf16e46eb4b7161d47ed6b19a863c5240";
    private static final String MERCHANT_ID = "3";

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
        String expected = sha256Hex(stringBuilder);
        System.out.println(expected);

        Assert.assertEquals(expected, encryptionUtil.formRequestSignature(paymentRequestDto, PASSWORD, MERCHANT_ID));
    }
}
