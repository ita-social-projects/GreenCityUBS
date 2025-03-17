package greencity.validators;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.entity.order.Order;
import greencity.exceptions.validation.ValidationException;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSManagementService;
import greencity.validators.payment.ManualPaymentRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static greencity.validators.payment.ManualPaymentRequestActions.UPDATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManualPaymentRequestValidatorTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UBSManagementService ubsManagementService;
    @InjectMocks
    private ManualPaymentRequestValidator validator;

    private ManualPaymentRequestDto requestDto;

    private static final long ORDER_ID = 1L;
    private static final String SETTLEMENT_DATE_FORMAT = "yyyy-MM-dd";

    @BeforeEach
    void setUp() {
        requestDto = new ManualPaymentRequestDto();
    }

    @Test
    void isValidTrueForValidPaymentDateTest() {
        LocalDateTime date = LocalDateTime.now();
        Order order = new Order();
        order.setOrderDate(date.minusDays(1));
        requestDto.setSettlementDate(date.format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.getOrderByPaymentId(ORDER_ID)).thenReturn(order);
        assertDoesNotThrow(() -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void isValidFalseForFutureSettlementDateTest() {
        LocalDateTime date = LocalDateTime.now();
        requestDto.setSettlementDate(date.plusDays(1).format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void isValidFalseForPastSettlementDateTest() {
        LocalDateTime date = LocalDateTime.now();
        Order order = new Order();
        order.setOrderDate(date.minusDays(1));
        requestDto.setSettlementDate(date.minusDays(2).format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.getOrderByPaymentId(ORDER_ID)).thenReturn(order);
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void isValidFalseForInvalidSettlementDateFormatsTest() {
        requestDto.setSettlementDate("17.03.2025");
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
        requestDto.setSettlementDate("17-03-2025");
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
        requestDto.setSettlementDate("17/03/2025");
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
        requestDto.setSettlementDate("03.17.2025");
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }
}
