package greencity.validators.payment;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.validation.ValidationException;
import greencity.service.ubs.UBSManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE;
import static greencity.constant.ValidationConstant.VALIDATION_RESPONSE_HEADER;
import static greencity.constant.ValidationConstant.VIOLATION_CHUNK;

/**
 * Validator for manual payment requests. Ensures that settlement dates are
 * valid and consistent with order creation dates.
 */
@Component
@RequiredArgsConstructor
public class ManualPaymentRequestValidator {
    private static final DateTimeFormatter SETTLEMENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final UBSManagementService ubsManagementService;

    public void validate(ManualPaymentRequestDto manualPaymentRequestDto,
        long id,
        ManualPaymentRequestActions action) {
        try {
            LocalDate settlementDateParsed = LocalDate.parse(
                manualPaymentRequestDto.getSettlementDate(), SETTLEMENT_DATE_FORMATTER);
            if (settlementDateParsed.isAfter(LocalDate.now())) {
                invalidate(PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE);
            }
            LocalDate orderDate = retrieveOrderDate(id, action);
            if (settlementDateParsed.isBefore(orderDate)) {
                invalidate(PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE);
            }
        } catch (DateTimeParseException exception) {
            invalidate(PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE);
        } catch (NotFoundException exception) {
            invalidate(exception.getMessage());
        }
    }

    private LocalDate retrieveOrderDate(long id, ManualPaymentRequestActions action) {
        return ManualPaymentRequestActions.ADD.equals(action)
            ? ubsManagementService.findOrderById(id).getDate()
            : ubsManagementService.getOrderByPaymentId(id).getDate();
    }

    private void invalidate(String violationMessage) {
        throw new ValidationException(VALIDATION_RESPONSE_HEADER + VIOLATION_CHUNK.formatted(violationMessage));
    }
}
