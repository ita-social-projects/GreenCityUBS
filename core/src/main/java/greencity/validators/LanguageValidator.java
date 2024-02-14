package greencity.validators;

import greencity.annotations.ValidLanguage;
import greencity.service.language.LanguageService;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;

@Slf4j
public class LanguageValidator implements ConstraintValidator<ValidLanguage, Locale> {
    private List<String> codes;
    private LanguageService languageService;

    @Override
    public void initialize(ValidLanguage constraintAnnotation) {
        try {
            codes = languageService.findAllLanguageCodes();
        } catch (Exception e) {
            codes = List.of("en", "ua");
            log.warn("Occurred error during processing request: {}", e.getMessage());
        }
    }

    @Override
    public boolean isValid(Locale value, ConstraintValidatorContext context) {
        return codes.contains(value.getLanguage());
    }
}
