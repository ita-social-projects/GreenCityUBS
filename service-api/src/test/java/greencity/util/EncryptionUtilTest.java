package greencity.util;

import greencity.dto.payment.PaymentRequestDto;
import greencity.dto.payment.PaymentResponseWayForPay;
import java.util.Arrays;
import java.util.StringJoiner;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EncryptionUtilTest {
    private EncryptionUtil encryptionUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        encryptionUtil = new EncryptionUtil();
    }

    @Test
    void testFormRequestSignature() {
        PaymentRequestDto dto = PaymentRequestDto.builder()
            .merchantAccount("merchant123")
            .merchantDomainName("example.com")
            .orderReference("order456")
            .orderDate(1213124124124L)
            .amount(2)
            .currency("UAH")
            .productName(Arrays.asList("Product1", "Product2"))
            .productCount(Arrays.asList(1, 2))
            .productPrice(Arrays.asList(50, 50))
            .build();

        String password = "testPassword";
        String merchantId = "merchantId";

        String signature = encryptionUtil.formRequestSignature(dto, password, merchantId);

        StringJoiner stringJoiner = new StringJoiner(";");
        stringJoiner.add(dto.getMerchantAccount())
            .add(dto.getMerchantDomainName())
            .add(dto.getOrderReference())
            .add(String.valueOf(dto.getOrderDate()))
            .add(String.valueOf(dto.getAmount()))
            .add(dto.getCurrency());
        dto.getProductName().forEach(stringJoiner::add);
        dto.getProductCount().forEach(count -> stringJoiner.add(String.valueOf(count)));
        dto.getProductPrice().forEach(price -> stringJoiner.add(String.valueOf(price)));

        String expectedSignature = new HmacUtils("HmacMD5", password).hmacHex(stringJoiner.toString());

        assertEquals(expectedSignature, signature);
    }

    @Test
    void testFormResponseSignature() {
        PaymentResponseWayForPay dto = PaymentResponseWayForPay.builder()
            .orderReference("order456")
            .status("success")
            .time("2023-07-19T12:00:00")
            .build();

        String password = "testPassword";

        String signature = encryptionUtil.formResponseSignature(dto, password);

        StringJoiner stringJoiner = new StringJoiner(";");
        stringJoiner.add(dto.getOrderReference())
            .add(dto.getStatus())
            .add(dto.getTime());
        String expectedSignature = new HmacUtils("HmacMD5", password).hmacHex(stringJoiner.toString());

        assertEquals(expectedSignature, signature);
    }
}
