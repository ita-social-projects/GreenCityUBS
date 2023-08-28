package greencity.dto.tariff;

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
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AddNewTariffDtoTest {

    @Test
    void addNewTariffDtoWithValidFieldsTest() {
        var dto = ModelUtils.getAddNewTariffDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void addNewTariffDtoWithValidNullFieldsTest() {
        var dto = ModelUtils.getAddNewTariffWithNullFieldsDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void addNewTariffDtoWithInvalidValuesTest(Long courierId, List<Long> locationIdList,
        List<Long> receivingStationsIdList) {
        AddNewTariffDto dto = new AddNewTariffDto(
            0L,
            courierId,
            locationIdList,
            receivingStationsIdList);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(4);
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of(null, List.of(), List.of()),
            Arguments.of(-2L, List.of(), List.of()),
            Arguments.of(0L, List.of(), List.of()));
    }
}
