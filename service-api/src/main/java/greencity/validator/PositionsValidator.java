package greencity.validator;

import greencity.annotations.ValidPositions;
import greencity.entity.user.employee.Position;
import greencity.repository.PositionRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
import static greencity.constant.ErrorMessage.INVALID_POSITION_IDS;

/**
 * Validator implementation for the {@link ValidPositions} annotation.
 * <p>
 * Validates that all position IDs in the given {@code Set<Long>} exist in the
 * database by checking against the {@link PositionRepository}.
 * </p>
 */
public class PositionsValidator implements ConstraintValidator<ValidPositions, Set<Long>> {
    private final PositionRepository positionRepository;

    /**
     * Constructs a {@code PositionsValidator} with the given
     * {@link PositionRepository}.
     *
     * @param positionRepository repository used to verify existence of position IDs
     */
    public PositionsValidator(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    /**
     * Validates that all position IDs in the provided set exist in the repository.
     * <p>
     * If any position IDs are missing, adds a custom constraint violation message
     * listing the missing IDs.
     * </p>
     *
     * @param positionIds                the set of position IDs to validate
     * @param constraintValidatorContext context in which the constraint is
     *                                   evaluated
     * @return {@code true} if all position IDs exist; {@code false} otherwise
     */
    @Override
    public boolean isValid(Set<Long> positionIds, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        Set<Long> existingIds = new HashSet<>(positionRepository.findAllById(positionIds))
            .stream()
            .map(Position::getId)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);

        if (existingIds.size() != positionIds.size()) {
            Set<Long> missingIds = new HashSet<>(positionIds);
            missingIds.removeAll(existingIds);

            constraintValidatorContext.buildConstraintViolationWithTemplate(
                String.format(INVALID_POSITION_IDS, missingIds))
                .addConstraintViolation();

            return false;
        }

        return true;
    }
}
