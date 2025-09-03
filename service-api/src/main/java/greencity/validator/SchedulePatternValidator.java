package greencity.validator;

import greencity.annotations.ValidSchedulePattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.CronExpression;

@RequiredArgsConstructor
public class SchedulePatternValidator implements ConstraintValidator<ValidSchedulePattern, String> {
    @Override
    public boolean isValid(String schedulePattern, ConstraintValidatorContext context) {
        if (schedulePattern != null) {
            return CronExpression.isValidExpression(schedulePattern);
        }

        return true;
    }
}
