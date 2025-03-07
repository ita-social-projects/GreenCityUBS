package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SettlementDateValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    private static final SettlementDateValidator validator = new SettlementDateValidator();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Test
    public void validateValidSettlementDate() {
        LocalDate localDate = LocalDate.now();
        assertTrue(validator.isValid(formatter.format(localDate), context));
        localDate = LocalDate.of(2020, 1, 1);
        assertTrue(validator.isValid(formatter.format(localDate), context));
        localDate = LocalDate.now().minusDays(1);
        assertTrue(validator.isValid(formatter.format(localDate), context));
    }
    @Test
    public void validateInvalidSettlementDate() {
        LocalDate localDate = LocalDate.now().plusDays(1);
        assertFalse(validator.isValid(formatter.format(localDate), context));
        localDate = LocalDate.now().plusMonths(1);
        assertFalse(validator.isValid(formatter.format(localDate), context));
        localDate = LocalDate.now().plusYears(1);
        assertFalse(validator.isValid(formatter.format(localDate), context));
        String wrongFormatDate = "05-09-2020";
        assertFalse(validator.isValid(wrongFormatDate, context));
        assertFalse(validator.isValid(null, context));
    }
}
