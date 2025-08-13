package greencity.annotations;

import greencity.validator.UpdateOrderPageAdminValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UpdateOrderPageAdminValidator.class})
public @interface ValidUpdateOrderPageAdmin {
    String message() default "{ValidUpdateOrderPageAdmin.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
