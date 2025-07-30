package greencity.validator;

import greencity.annotations.ValidTariffs;
import greencity.dto.tariff.TariffWithChatAccess;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TariffsValidator implements ConstraintValidator<ValidTariffs, List<TariffWithChatAccess>> {
    @Override
    public boolean isValid(List<TariffWithChatAccess> tariffs, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        if (tariffs == null) {
            context.buildConstraintViolationWithTemplate("tariffs cannot be null")
                .addConstraintViolation();
            return false;
        }
        if (tariffs.isEmpty()) {
            context.buildConstraintViolationWithTemplate("tariffs cannot be empty")
                .addConstraintViolation();
            return false;
        }

        Set<Long> uniqueIds = new HashSet<>();

        for (TariffWithChatAccess tariff : tariffs) {
            if (tariff == null) {
                context.buildConstraintViolationWithTemplate("tariff cannot be null")
                    .addConstraintViolation();
                return false;
            }

            Long tariffId = tariff.getTariffId();

            if (tariffId == null) {
                context.buildConstraintViolationWithTemplate("tariffId cannot be null")
                    .addConstraintViolation();
                return false;
            }

            if (!uniqueIds.add(tariffId)) {
                context.buildConstraintViolationWithTemplate("tariffs cannot contain duplicates")
                    .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
