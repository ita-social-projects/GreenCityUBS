package greencity.validator;

import static greencity.constant.ErrorMessage.TARIFF_LIST_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFF_LIST_IS_EMPTY;
import static greencity.constant.ErrorMessage.TARIFF_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFFID_IS_NULL;
import static greencity.constant.ErrorMessage.TARIFFID_IS_NOT_POSITIVE;
import static greencity.constant.ErrorMessage.TARIFF_LIST_CONTAINS_DUPLICATES;
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
            context.buildConstraintViolationWithTemplate(TARIFF_LIST_IS_NULL)
                .addConstraintViolation();
            return false;
        }
        if (tariffs.isEmpty()) {
            context.buildConstraintViolationWithTemplate(TARIFF_LIST_IS_EMPTY)
                .addConstraintViolation();
            return false;
        }

        Set<Long> uniqueIds = new HashSet<>();

        for (TariffWithChatAccess tariff : tariffs) {
            if (tariff == null) {
                context.buildConstraintViolationWithTemplate(TARIFF_IS_NULL)
                    .addConstraintViolation();
                return false;
            }

            Long tariffId = tariff.getTariffId();

            if (tariffId == null) {
                context.buildConstraintViolationWithTemplate(TARIFFID_IS_NULL)
                    .addConstraintViolation();
                return false;
            }

            if (tariffId < 1L) {
                context.buildConstraintViolationWithTemplate(TARIFFID_IS_NOT_POSITIVE)
                    .addConstraintViolation();
                return false;
            }

            if (!uniqueIds.add(tariffId)) {
                context.buildConstraintViolationWithTemplate(TARIFF_LIST_CONTAINS_DUPLICATES)
                    .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}