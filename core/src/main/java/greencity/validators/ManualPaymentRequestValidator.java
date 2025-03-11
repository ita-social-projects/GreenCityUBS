package greencity.validators;

import greencity.annotations.ValidManualPaymentRequest;
import greencity.dto.payment.ManualPaymentRequestDto;
import greencity.entity.order.Order;
import greencity.repository.OrderRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE;
import static greencity.constant.ValidationConstant.PAYMENT_DATE_IS_NULL_MESSAGE;

@RequiredArgsConstructor
public class ManualPaymentRequestValidator
    implements ConstraintValidator<ValidManualPaymentRequest, ManualPaymentRequestDto> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final OrderRepository orderRepository;

    @Override
    public boolean isValid(ManualPaymentRequestDto manualPaymentRequestDto,
        ConstraintValidatorContext constraintValidatorContext) {
        String settlementDate = manualPaymentRequestDto.getSettlementDate();
        System.out.println(settlementDate);
        if (settlementDate == null) {
            return setValidViolationMessages(constraintValidatorContext, PAYMENT_DATE_IS_NULL_MESSAGE);
        }
        try {
            if (LocalDate.parse(settlementDate, formatter).isAfter(LocalDate.now())) {
                return setValidViolationMessages(constraintValidatorContext,
                    PAYMENT_DATE_IS_AFTER_CURRENT_DATE_MESSAGE);
            }
            Order order = orderRepository.findOrderByPaymentId(manualPaymentRequestDto.getPaymentId());
            if (LocalDate.parse(settlementDate, formatter).isBefore(order.getOrderDate().toLocalDate())) {
                return setValidViolationMessages(constraintValidatorContext,
                    PAYMENT_DATE_IS_BEFORE_ORDER_CREATION_MESSAGE);
            }
        } catch (DateTimeParseException e) {
            return setValidViolationMessages(constraintValidatorContext, PAYMENT_DATE_FORMAT_IS_NOT_VALID_MESSAGE);
        }
        return true;
    }

    private boolean setValidViolationMessages(ConstraintValidatorContext constraintValidatorContext,
        String violationMessage) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        constraintValidatorContext.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
        return false;
    }
}
