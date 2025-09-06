package greencity.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulePatternValidatorTest {
    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @InjectMocks
    private SchedulePatternValidator validator;

    @ParameterizedTest
    @ValueSource(strings = {"* * * * * *", "* * * * * *", "0 15 10 * * ?", "0 15 10 ? * 6L",
        "0/5 14,18,3-39,52 * ? JAN,MAR,SEP MON-FRI"})
    void isValidWithValidPattern(String pattern) {
        boolean actual = validator.isValid(pattern, context);
        assertTrue(actual);
    }

    @Test
    void isValidWithNullPattern() {
        boolean actual = validator.isValid(null, context);
        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "pattern", "* * * * * * * *", "******", "100 * * * * *",
        "* 100 * * * *", "* * 100 * * *", "* * * 100 * *", "* * * * 100 *", "* * * * UNK *",
        "* * * * * 100", "* * * * * UNK"})
    void isValidWithInvalidPattern(String pattern) {
        boolean actual = validator.isValid(pattern, context);
        assertFalse(actual);
    }
}
