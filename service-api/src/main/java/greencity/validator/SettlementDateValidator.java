package greencity.validator;

import greencity.annotations.ValidSettlementDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SettlementDateValidator implements ConstraintValidator<ValidSettlementDate, String> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public boolean isValid(String settlementDateToValidate, ConstraintValidatorContext constraintValidatorContext) {
        if (settlementDateToValidate == null) {
            return false;
        }
        try {
            return LocalDate.parse(settlementDateToValidate, formatter).isBefore(LocalDate.now().plusDays(1));
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
