package greencity.validator;

import greencity.annotations.ValidPositions;
import greencity.dto.position.PositionDto;
import greencity.service.ubs.PositionService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Component;
import static greencity.constant.ErrorMessage.INVALID_POSITION_IDS;

/**
 * Validator implementation for the {@link ValidPositions} annotation.
 * <p>
 * Validates that all position IDs in the given {@code Set<Long>} exist in the
 * database by checking against the {@link PositionService}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PositionsValidator implements ConstraintValidator<ValidPositions, Set<Long>> {
    private final PositionService positionService;

    /**
     * Validates that all position IDs in the provided set exist in the repository.
     * <p>
     * If any position IDs are missing, adds a custom constraint violation message
     * listing the missing IDs.
     * </p>
     *
     * @param positionIds the set of position IDs to validate
     * @param ctx         context in which the constraint is evaluated
     * @return {@code true} if all position IDs exist; {@code false} otherwise
     */
    @Override
    public boolean isValid(Set<Long> positionIds, ConstraintValidatorContext ctx) {
        if (positionIds == null || positionIds.isEmpty()) {
            return true;
        }

        Set<Long> existingIds = positionService.findAllByIds(positionIds).stream()
            .map(PositionDto::getId)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);

        if (existingIds.size() != positionIds.size()) {
            Set<Long> missingIds = new TreeSet<>(positionIds);
            missingIds.removeAll(existingIds);

            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                String.format(INVALID_POSITION_IDS, missingIds))
                .addConstraintViolation();

            return false;
        }

        return true;
    }
}
