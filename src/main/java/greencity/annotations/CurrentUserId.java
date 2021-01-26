package greencity.annotations;

import greencity.dao.entity.user.User;
import greencity.converters.UserArgumentResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation is used for injecting  id of {@link User} into
 * controller by {@link UserArgumentResolver}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CurrentUserId {
}
