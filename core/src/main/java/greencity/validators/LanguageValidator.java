package greencity.validators;

import greencity.annotations.ValidLanguage;
import greencity.service.language.LanguageService;
import java.util.List;
import java.util.Locale;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class LanguageValidator implements ConstraintValidator<ValidLanguage, Locale> {
    private List<String> codes;

    @Autowired
    private LanguageService languageService;

    @Override
    public void initialize(ValidLanguage constraintAnnotation) {
        codes = languageService.findAllLanguageCodes();
    }

    @Override
    public boolean isValid(Locale value, ConstraintValidatorContext context) {
        return codes.contains(value.getLanguage());
    }
}
