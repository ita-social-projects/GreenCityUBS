package greencity.annotations;

import greencity.validator.PositionsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static greencity.constant.ErrorMessage.POSITION_VALIDATION_ERROR_MESSAGE;

/**
 * Custom validation annotation to check if the positions in a field are valid
 * according to the {@link PositionsValidator} logic.
 * <p>
 * This annotation can be applied to fields and is retained at runtime. It uses
 * {@link PositionsValidator} to perform the validation.
 * </p>
 */
@Constraint(validatedBy = PositionsValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPositions {
    /**
     * The default validation error message if the validation fails.
     *
     * @return the error message string
     */
    String message() default POSITION_VALIDATION_ERROR_MESSAGE;

    /**
     * Allows specification of validation groups, to which this constraint belongs.
     *
     * @return the groups this constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients to specify additional information about the validation
     * failure.
     *
     * @return the payload classes associated with the constraint
     */
    Class<? extends Payload>[] payload() default {};
}
