package greencity.dto.courier;

import greencity.ModelUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCourierDtoTest {

    @Test
    void createCourierDtoWithValidFieldsTest() {
        var dto = ModelUtils.getCreateCourierDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void createCourierDtoWithInvalidFieldsTest(String nameEn, String nameUk) {
        var dto = new CreateCourierDto(nameEn, nameUk);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("", ""),
            Arguments.of("@#$", "@#$"),
            Arguments.of("Тест", "Test"),
            Arguments.of("Test111111111111111111111111111", "Test111111111111111111111111111"));
    }
}
