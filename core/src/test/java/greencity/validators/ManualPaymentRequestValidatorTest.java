package greencity.validators;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSManagementService;
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
    @Mock
    private UBSManagementService ubsManagementService;
    @InjectMocks
    private ManualPaymentRequestValidator validator;

    private ManualPaymentRequestDto requestDto;

    private static final String ORDER_ID = "1";

    @BeforeEach
    void setUp() {
        requestDto = new ManualPaymentRequestDto();
    }

    @Test
    void isValidTrueForValidPaymentDateTest() {
        requestDto.setSettlementDate(LocalDate.now().toString());
        requestDto.setPaymentId(ORDER_ID);
        Order order = new Order();
        order.setOrderDate(LocalDate.now().minusDays(1).atStartOfDay());
        when(ubsManagementService.getOrderByPaymentId(anyString())).thenReturn(order);
        assertTrue(validator.isValid(requestDto, context));
    }

    @Test
    void isValidFalseForNullSettlementDateTest() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(null);
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void isValidFalseForFutureSettlementDateTest() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(LocalDate.now().plusDays(1).toString());
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void isValidFalseForPastSettlementDateTest() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        requestDto.setSettlementDate(LocalDate.now().minusDays(10).toString());
        requestDto.setPaymentId(ORDER_ID);
        Order order = new Order();
        order.setOrderDate(LocalDate.now().minusDays(5).atStartOfDay());
        when(ubsManagementService.getOrderByPaymentId(anyString())).thenReturn(order);
        assertFalse(validator.isValid(requestDto, context));
    }

    @Test
    void isValidFalseForInvalidSettlementDateFormatsTest() {
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
