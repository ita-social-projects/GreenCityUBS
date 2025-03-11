package greencity.validators;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManualPaymentRequestValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    @Mock
    private ConstraintViolationBuilder violationBuilder;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private ManualPaymentRequestValidator validator;

    private ManualPaymentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new ManualPaymentRequestDto();
    }

    @Test
    void testValidPaymentDate() {
        requestDto.setSettlementDate(LocalDate.now().toString());
        requestDto.setPaymentId("123");
        Order order = new Order();
        order.setOrderDate(LocalDate.now().minusDays(1).atStartOfDay());
        when(orderRepository.findOrderByPaymentId("123")).thenReturn(order);
        assertTrue(validator.isValid(requestDto, context));
    }

    @Test
    void testNullPaymentDate() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(null);
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void testFuturePaymentDate() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(LocalDate.now().plusDays(1).toString());
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void testPaymentDateBeforeOrderCreation() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(LocalDate.now().minusDays(10).toString());
        requestDto.setPaymentId("123");
        Order order = new Order();
        order.setOrderDate(LocalDate.now().minusDays(5).atStartOfDay());
        when(orderRepository.findOrderByPaymentId("123")).thenReturn(order);
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void testInvalidDateFormat() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate("invalid-date");
        assertFalse(validator.isValid(requestDto, context));
        requestDto.setSettlementDate("11.03.2025");
        assertFalse(validator.isValid(requestDto, context));
        requestDto.setSettlementDate("11/03/2025");
        assertFalse(validator.isValid(requestDto, context));
        requestDto.setSettlementDate("03.11.2025");
        assertFalse(validator.isValid(requestDto, context));
        requestDto.setSettlementDate("03-11-2025");
        assertFalse(validator.isValid(requestDto, context));
    }
}
