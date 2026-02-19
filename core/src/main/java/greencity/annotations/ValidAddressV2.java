package greencity.annotations;

import greencity.validators.AddressValidatorV2;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static greencity.constant.ValidationConstant.ADDRESS_VALIDATION_ERROR_MESSAGE;

@Constraint(validatedBy = AddressValidatorV2.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ValidAddressV2 {
    String message() default ADDRESS_VALIDATION_ERROR_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
