package greencity.util;

import greencity.ModelUtils;
import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EncryptionUtilTest {

    @InjectMocks
    EncryptionUtil encryptionUtil = new EncryptionUtil();
    private static final String PASSWORD = "One2three";
    private static final String INVALID_PASSWORD = "password";
    private static final String SIGNATURE = "c972ca8f1eb227d85631728d690037cfa41375af";
    private static final String MERCHANT_ID = "3";
    private static final String PRIVATE_KEY = "privateKey";

}
