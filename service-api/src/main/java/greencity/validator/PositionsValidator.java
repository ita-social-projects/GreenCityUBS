package greencity.validator;

import greencity.annotations.ValidPositions;
import greencity.entity.user.employee.Position;
import greencity.repository.PositionRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;
import static greencity.constant.ErrorMessage.INVALID_POSITION_IDS;

public class PositionsValidator implements ConstraintValidator<ValidPositions, Set<Long>> {
    private final PositionRepository positionRepository;

    public PositionsValidator(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

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
