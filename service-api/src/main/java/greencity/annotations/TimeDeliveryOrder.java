package greencity.annotations;

import greencity.validator.TimeDeliveryOrderValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = TimeDeliveryOrderValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeDeliveryOrder {
    /**
     * The message that will be shown when the validation fails.
     *
     * @return the default error message
     */
    String message() default "timeDeliveryFrom must be before timeDeliveryTo";

    /**
     * Allows you to specify validation groups, which can be used to apply different
     * validations in different scenarios.
     *
     * @return an array of groups to which this constraint belongs
     */
    Class<?>[] groups() default {};

    /**
     * Carries additional data that can be used during the validation process. This
     * attribute is primarily used by frameworks that support custom validation.
     *
     * @return an array of payload objects
     */
    Class<? extends Payload>[] payload() default {};
}