package greencity.validator;

import greencity.constant.ErrorMessage;
import greencity.dto.position.PositionDto;
import greencity.service.ubs.PositionService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PositionsValidatorTest {
    @Mock
    private PositionService positionService;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @InjectMocks
    private PositionsValidator validator;

    @Test
    void isValidShouldReturnTrueWhenAllPositionsExist() {
        Set<Long> inputIds = Set.of(1L, 2L);
        List<PositionDto> existingPositions = List.of(createPosition(1L), createPosition(2L));

        when(positionService.findAllByIds(inputIds)).thenReturn(existingPositions);

        assertTrue(validator.isValid(inputIds, context));
    }

    @Test
    void isValidShouldReturnFalseWhenSomePositionsDoNotExist() {
        Set<Long> inputIds = Set.of(1L, 2L, 3L);
        List<PositionDto> existingPositions = List.of(createPosition(1L), createPosition(2L));

        when(positionService.findAllByIds(inputIds)).thenReturn(existingPositions);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(validator.isValid(inputIds, context));
        verify(context).buildConstraintViolationWithTemplate(
            String.format(ErrorMessage.INVALID_POSITION_IDS, Set.of(3L)));
        verify(context).disableDefaultConstraintViolation();
    }

    @Test
    void isValidShouldReturnFalseWhenAllPositionsDoNotExist() {
        Set<Long> inputIds = Set.of(10L, 20L);
        List<PositionDto> existingPositions = Collections.emptyList();

        when(positionService.findAllByIds(inputIds)).thenReturn(existingPositions);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(validator.isValid(inputIds, context));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(captor.capture());

        String message = captor.getValue();
        assertThat(message).startsWith("Invalid position IDs: [");
        assertThat(message).contains("10");
        assertThat(message).contains("20");
    }

    @Test
    void isValidShouldReturnTrueWhenInputIsEmpty() {
        Set<Long> inputIds = Collections.emptySet();

        assertTrue(validator.isValid(inputIds, context));
        verifyNoInteractions(positionService);
    }

    @Test
    void isValidShouldReturnTrueWhenInputIsNull() {
        assertTrue(validator.isValid(null, context));
        verifyNoInteractions(positionService);
    }

    private PositionDto createPosition(Long id) {
        PositionDto position = mock(PositionDto.class);
        when(position.getId()).thenReturn(id);
        return position;
    }
}
