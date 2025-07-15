package greencity.util;

import greencity.entity.order.Order;
import greencity.entity.order.Payment;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderUtilsTest {
    private Order order;

    @BeforeEach
    void setUp() {
        order = mock(Order.class);
    }

    @Test
    void generateEncodedOrderReferenceTest() {
        Long orderId = 123L;
        Payment payment = mock(Payment.class);

        when(order.getPayment()).thenReturn(Collections.singletonList(payment));
        when(order.getCounterOrderPaymentId()).thenReturn(2L);
        when(payment.getId()).thenReturn(456L);

        String encodedOrderId = OrderUtils.generateEncodedOrderReference(orderId, order);

        String decodedOrderId = new String(Base64.getDecoder().decode(encodedOrderId));

        assertEquals("123_2_456", decodedOrderId);
    }

    @Test
    void decodeOrderReferenceTest() {
        String encodedOrderId = Base64.getEncoder().encodeToString("123_2_456".getBytes());
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
}
