package greencity.validators.payment;

import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.validation.ValidationException;
import greencity.service.ubs.UBSManagementService;
import greencity.validator.response.ManualPaymentRequestValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE;

@Component
@RequiredArgsConstructor
public class ManualPaymentRequestValidator {
    private static final DateTimeFormatter SETTLEMENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final UBSManagementService ubsManagementService;

    public void validate(ManualPaymentRequestDto manualPaymentRequestDto,
        long paymentId,
        ManualPaymentRequestActions action) {
        ManualPaymentRequestValidationResponse validationResponse = new ManualPaymentRequestValidationResponse();
        try {
            LocalDate settlementDateParsed = LocalDate.parse(
                manualPaymentRequestDto.getSettlementDate(), SETTLEMENT_DATE_FORMATTER);
            if (settlementDateParsed.isAfter(LocalDate.now())) {
                invalidate(validationResponse, PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE);
            }
            LocalDate orderDate;
            if (ManualPaymentRequestActions.ADD.equals(action)) {
                orderDate = ubsManagementService.findOrderById(paymentId).getOrderDate().toLocalDate();
            } else {
                orderDate = ubsManagementService.getOrderByPaymentId(paymentId).getOrderDate().toLocalDate();
            }
            if (settlementDateParsed.isBefore(orderDate)) {
                invalidate(validationResponse, PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE);
            }
        } catch (DateTimeParseException exception) {
            invalidate(validationResponse, PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE);
        } catch (NotFoundException exception) {
            invalidate(validationResponse, exception.getMessage());
        }
    }

    private void invalidate(ManualPaymentRequestValidationResponse validationResponse, String violationMessage) {
        validationResponse.invalidate();
        validationResponse.setViolationMessage(violationMessage);
        throw new ValidationException(validationResponse.toString());
    }
}
