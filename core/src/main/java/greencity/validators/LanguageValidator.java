package greencity.validators;

import greencity.annotations.ValidLanguage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;

public class LanguageValidator implements ConstraintValidator<ValidLanguage, Locale> {
    private List<String> codes;

    @Override
    public void initialize(ValidLanguage constraintAnnotation) {
        codes = List.of("en", "ua");
    }

    @Override
    public boolean isValid(Locale value, ConstraintValidatorContext context) {
        return codes.contains(value.getLanguage());
    }
}
