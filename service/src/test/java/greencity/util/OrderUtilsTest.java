package greencity.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import greencity.entity.order.Order;
import greencity.entity.order.Payment;
import greencity.exceptions.payment.InvalidPaymentResponseException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderUtilsTest {
    private Order order;

    @BeforeEach
    void setUp() {
        order = mock(Order.class);
    }

    @Test
    void generateEncodedOrderReferenceTest() {
        Payment payment = mock(Payment.class);

        when(order.getId()).thenReturn(123L);
        when(order.getPayment()).thenReturn(Collections.singletonList(payment));
        when(order.getCounterOrderPaymentId()).thenReturn(2L);
        when(payment.getId()).thenReturn(456L);

        String encodedOrderId = OrderUtils.generateEncodedOrderReference(order);

        String decodedOrderId = new String(Base64.getDecoder().decode(encodedOrderId));

        assertEquals("123_2_456", decodedOrderId);
    }

    @Test
    void decodeOrderReferenceTest() {
        String encodedOrderId = Base64.getEncoder().withoutPadding().encodeToString("123_2_456".getBytes());
        Payment payment = mock(Payment.class);

        when(order.getPayment()).thenReturn(Collections.singletonList(payment));
        when(order.getCounterOrderPaymentId()).thenReturn(2L);
        when(payment.getId()).thenReturn(456L);

        String decodedOrderId = OrderUtils.decodeOrderReference(encodedOrderId);

        assertEquals("123_2_456", decodedOrderId);
    }

    @Test
    void getLastPaymentTest() {
        List<Payment> payments = new ArrayList<>();
        payments.add(new Payment());
        payments.add(new Payment().setId(5L));
        payments.add(new Payment().setId(2L));

        when(order.getPayment()).thenReturn(payments);

        Payment payment = OrderUtils.getLastPayment(order);

        assertEquals(5L, payment.getId());
    }

    @Test
    void getIdByOrderReference_validBase64_returnsCorrectId() {
        String encoded = Base64.getEncoder().encodeToString("123_456_789".getBytes(StandardCharsets.UTF_8));

        Long result0 = OrderUtils.getIdByOrderReference(encoded, 0);
        Long result1 = OrderUtils.getIdByOrderReference(encoded, 1);
        Long result2 = OrderUtils.getIdByOrderReference(encoded, 2);

        assertEquals(123L, result0);
        assertEquals(456L, result1);
        assertEquals(789L, result2);
    }

    @Test
    void getIdByOrderReference_indexOutOfBounds_throwsException() {
        String encoded = Base64.getEncoder().encodeToString("123_456_789".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidPaymentResponseException.class,
            () -> OrderUtils.getIdByOrderReference(encoded, 3));
    }

    @Test
    void getIdByOrderReference_nonNumericPart_throwsException() {
        String encoded = Base64.getEncoder().encodeToString("123_abc_789".getBytes(StandardCharsets.UTF_8));

        assertThrows(InvalidPaymentResponseException.class,
            () -> OrderUtils.getIdByOrderReference(encoded, 1));
    }
}
