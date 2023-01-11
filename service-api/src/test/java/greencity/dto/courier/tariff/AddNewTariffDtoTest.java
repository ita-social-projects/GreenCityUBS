package greencity.dto.courier.tariff;

import greencity.ModelUtils;
import greencity.dto.AddNewTariffDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AddNewTariffDtoTest {

    @Test
    void givenValidDtoWhenValidatedThenNoValidationError() {
        var dto = ModelUtils.getAddNewTariffDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
                validator.validate(dto);

        assertThat(constraintViolations.size()).isZero();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldAndInvalidValue")
    void testInvalidDto(String fieldName, Object invalidValue) {
        var dto = ModelUtils.getAddNewTariffDto();

        Field field = AddNewTariffDto.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(dto, invalidValue);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
                validator.validate(dto);

        assertThat(constraintViolations.size()).isOne();
    }

    private static Stream<Arguments> provideFieldAndInvalidValue() {
        return Stream.of(
                Arguments.of("courierId", null),
                Arguments.of("locationIdList", Collections.EMPTY_LIST),
                Arguments.of("receivingStationsIdList", null),
                Arguments.of("regionId", null)
        );
    }
}
