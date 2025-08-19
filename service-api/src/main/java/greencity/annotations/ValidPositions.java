package greencity.annotations;

import greencity.validator.PositionsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static greencity.constant.ErrorMessage.POSITION_VALIDATION_ERROR_MESSAGE;

@Constraint(validatedBy = PositionsValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPositions {
    String message() default POSITION_VALIDATION_ERROR_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
