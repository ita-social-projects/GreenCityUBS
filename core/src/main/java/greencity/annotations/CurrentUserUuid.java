package greencity.annotations;

import greencity.converters.UserArgumentResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used for injecting id of {@link greencity.entity.user.User}
 * into controller by {@link UserArgumentResolver}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentUserUuid {
}
