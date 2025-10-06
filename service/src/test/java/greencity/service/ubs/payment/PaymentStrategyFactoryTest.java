package greencity.service.ubs.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import greencity.enums.PaymentSystem;
import greencity.service.ubs.wayforpay.WayForPayStrategy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentStrategyFactoryTest {
    @Mock
    private WayForPayStrategy wayForPayStrategy;

    private PaymentStrategyFactory paymentStrategyFactory;

    @BeforeEach
    void setUp() {
        when(wayForPayStrategy.getPaymentSystem()).thenReturn(PaymentSystem.WAY_FOR_PAY);

        paymentStrategyFactory = new PaymentStrategyFactory(List.of(wayForPayStrategy));
    }

    @Test
    void shouldReturnWayForPayStrategy_WhenRequested() {
        PaymentStrategy result = paymentStrategyFactory.getPaymentStrategy(PaymentSystem.WAY_FOR_PAY);

        assertThat(result).isEqualTo(wayForPayStrategy);
    }

    @Test
    void shouldReturnNull_WhenPaymentSystemNotRegistered() {
        PaymentStrategy result = paymentStrategyFactory.getPaymentStrategy(null);

        assertThat(result).isNull();
    }

    @Test
    void shouldStoreStrategyInMap() {
        Map<PaymentSystem, PaymentStrategy> strategies =
            (Map<PaymentSystem, PaymentStrategy>) ReflectionTestUtils.getField(paymentStrategyFactory,
                "strategies");

        assertThat(strategies)
            .hasSize(1)
            .containsEntry(PaymentSystem.WAY_FOR_PAY, wayForPayStrategy);
    }
}