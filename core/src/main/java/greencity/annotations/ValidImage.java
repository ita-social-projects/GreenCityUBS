package greencity.annotations;

import greencity.validators.ImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating that uploaded file(s) are of image MIME type.
 * <p>
 * Can be applied to a single
 * {@link org.springframework.web.multipart.MultipartFile} or an array of
 * {@code MultipartFile[]} to ensure that the content type of each file starts
 * with {@code "image/"} (e.g. {@code image/png}, {@code image/jpeg}).
 * </p>
 * <p>
 * Validation passes if:
 * <ul>
 * <li>The value is {@code null} (use {@code @NotNull} to enforce
 * presence).</li>
 * <li>The file (or all files in the array) has a valid image MIME type.</li>
 * </ul>
 * </p>
 * <p>
 * This annotation leverages the
 * {@link greencity.validators.ImageValidator.SingleImageValidator} and
 * {@link greencity.validators.ImageValidator.ArrayImageValidator}
 * implementations for validation logic.
 * </p>
 */
@Constraint(validatedBy = {ImageValidator.SingleImageValidator.class, ImageValidator.ArrayImageValidator.class})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface ValidImage {
    /**
     * The validation error message that is returned if the file type is invalid.
     *
     * @return the validation error message
     */
    String message() default "Invalid file type. Only image files are allowed.";

    /**
     * Allows the specification of validation groups, to which this constraint
     * belongs.
     *
     * @return array of group classes
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients to specify additional metadata information about the
     * validation failure.
     *
     * @return array of payload classes
     */
    Class<? extends Payload>[] payload() default {};
}
