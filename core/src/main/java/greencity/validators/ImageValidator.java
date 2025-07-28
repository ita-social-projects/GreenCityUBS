package greencity.validators;

import greencity.annotations.ValidImage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator classes for the {@link greencity.annotations.ValidImage}
 * annotation.
 * <p>
 * Contains inner validator implementations to provide validation logic to check
 * whether uploaded file(s) have an image MIME type. Supports validation of both
 * single {@link MultipartFile} instances and arrays of {@code MultipartFile[]}.
 * </p>
 */
public class ImageValidator {
    /**
     * Validator for a single {@link MultipartFile}.
     * <p>
     * Validation passes if:
     * <ul>
     * <li>The file is {@code null} (null values are considered valid).</li>
     * <li>The content type of the file is non-null and starts with
     * {@code "image/"}, indicating an image MIME type.</li>
     * </ul>
     * Otherwise, the validation fails.
     * </p>
     */
    public static class SingleImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {
        @Override
        public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
            if (file == null) {
                return true;
            }

            String mimeType = file.getContentType();
            return mimeType != null && mimeType.startsWith("image/");
        }
    }

    /**
     * Validator for an array of {@link MultipartFile} objects.
     * <p>
     * Validation passes if:
     * <ul>
     * <li>The array is {@code null} (null values are considered valid).</li>
     * <li>Each file in the array is either {@code null} or has a content type that
     * starts with {@code "image/"}, indicating an image MIME type.</li>
     * </ul>
     * Validation fails if any non-null file has a content type that does not
     * indicate an image.
     * </p>
     */
    public static class ArrayImageValidator implements ConstraintValidator<ValidImage, MultipartFile[]> {
        @Override
        public boolean isValid(MultipartFile[] files, ConstraintValidatorContext constraintValidatorContext) {
            if (files == null) {
                return true;
            }

            for (MultipartFile file : files) {
                if (file == null) {
                    continue;
                }

                String mimeType = file.getContentType();
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    return false;
                }
            }

            return true;
        }
    }
}
