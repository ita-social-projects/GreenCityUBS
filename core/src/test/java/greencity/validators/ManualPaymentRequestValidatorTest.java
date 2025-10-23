package greencity.validators;

import greencity.dto.order.OrderDetailStatusDto;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.entity.order.Order;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.validation.ValidationException;
import greencity.repository.OrderRepository;
import greencity.service.ubs.UBSManagementService;
import greencity.validators.payment.ManualPaymentRequestValidator;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static greencity.validators.payment.ManualPaymentRequestActions.ADD;
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
    void validateUpdateForValidPaymentDateTest() {
        LocalDateTime date = LocalDateTime.now();
        OrderDetailStatusDto orderDetails = OrderDetailStatusDto.builder()
            .date(LocalDate.now().minusDays(1))
            .build();
        requestDto.setSettlementDate(date.format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.getOrderByPaymentId(ORDER_ID)).thenReturn(orderDetails);
        assertDoesNotThrow(() -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void validateAddForValidPaymentDateTest() {
        LocalDateTime date = LocalDateTime.now();
        OrderDetailStatusDto orderDetails = OrderDetailStatusDto.builder()
            .date(LocalDate.now().minusDays(1))
            .build();
        requestDto.setSettlementDate(date.format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.findOrderById(ORDER_ID)).thenReturn(orderDetails);
        assertDoesNotThrow(() -> validator.validate(requestDto, ORDER_ID, ADD));
    }

    @Test
    void validateUpdateDoesThrowExceptionForFutureSettlementDateTest() {
        LocalDateTime date = LocalDateTime.now();
        requestDto.setSettlementDate(date.plusDays(1).format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void validateUpdateDoesThrowExceptionIfOrderNotFoundTest() {
        LocalDateTime date = LocalDateTime.now();
        requestDto.setSettlementDate(date.minusDays(2).format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.getOrderByPaymentId(ORDER_ID)).thenThrow(new NotFoundException());
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void validateUpdateDoesThrowExceptionForPastSettlementDateTest() {
        LocalDateTime date = LocalDateTime.now();
        OrderDetailStatusDto orderDetails = OrderDetailStatusDto.builder()
            .date(LocalDate.now().minusDays(1))
            .build();
        requestDto.setSettlementDate(date.minusDays(2).format(DateTimeFormatter.ofPattern(SETTLEMENT_DATE_FORMAT)));
        when(ubsManagementService.getOrderByPaymentId(ORDER_ID)).thenReturn(orderDetails);
        assertThrows(ValidationException.class, () -> validator.validate(requestDto, ORDER_ID, UPDATE));
    }

    @Test
    void validateUpdateDoesThrowExceptionForInvalidSettlementDateFormatsTest() {
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
