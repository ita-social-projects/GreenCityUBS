package greencity.annotations;

import greencity.validator.PhoneNumberValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used for validating phone number {@link String}.
 */
@Constraint(validatedBy = PhoneNumberValidation.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidPhoneNumber {
    /**
     * Defines the message that will be showed when the input data is not valid.
     *
     * @return message
     */
    String message() default "Invalid phone number format.";

    /**
     * Let you select to split the annotations into different groups to apply
     * different validations to each group.
     *
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     * Payloads are typically used to carry metadata information consumed by a
     * validation client.
     *
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
}
