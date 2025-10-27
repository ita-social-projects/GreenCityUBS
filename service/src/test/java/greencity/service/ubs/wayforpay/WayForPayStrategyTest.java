package greencity.service.ubs.wayforpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import greencity.ModelUtils;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.PaymentSystemResponse;
import greencity.entity.order.Order;
import greencity.enums.PaymentSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WayForPayStrategyTest {
    @Mock
    private WayForPayService wayForPayService;

    @InjectMocks
    private WayForPayStrategy wayForPayStrategy;

    @Test
    void getPaymentSystem_ShouldReturnWayForPay() {
        var result = wayForPayStrategy.getPaymentSystem();

        assertEquals(PaymentSystem.WAY_FOR_PAY, result);
    }

    @Test
    void processPayment_ShouldDelegateToWayForPayService() {
        Order order = ModelUtils.getOrder();
        long sumToPayInCoins = 100L;
        PaymentSystemResponse expectedResponse = ModelUtils.getPaymentSystemResponse();
        OrderResponseDto orderResponseDto = ModelUtils.getOrderResponseDto();

        when(wayForPayService.processWayForPay(orderResponseDto, order.getId(), sumToPayInCoins))
            .thenReturn(expectedResponse);

        PaymentSystemResponse actualResponse =
            wayForPayStrategy.processPayment(orderResponseDto, order.getId(), sumToPayInCoins);

        assertEquals(expectedResponse, actualResponse);
        verify(wayForPayService).processWayForPay(orderResponseDto, order.getId(), sumToPayInCoins);
        verifyNoMoreInteractions(wayForPayService);
    }
}