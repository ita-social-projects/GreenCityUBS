package greencity.validators;

import greencity.annotations.ValidImage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ImageValidator {
    public static class Single implements ConstraintValidator<ValidImage, MultipartFile> {
        @Override
        public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
            if (file == null) {
                return true;
            }

            String mimeType = file.getContentType();
            return mimeType != null && mimeType.startsWith("image/");
        }
    }

    public static class Array implements ConstraintValidator<ValidImage, MultipartFile[]> {
        @Override
        public boolean isValid(MultipartFile[] files, ConstraintValidatorContext constraintValidatorContext) {
            if (files == null) {
                return true;
            }

            for (MultipartFile file : files) {
                if  (file == null) {
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
