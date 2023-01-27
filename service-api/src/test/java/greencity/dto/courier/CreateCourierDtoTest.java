package greencity.dto.courier;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCourierDtoTest {

    @Test
    void createCourierDtoWithValidFieldsTest() {
        var dto = ModelUtils.getCreateCourierDto();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void createCourierDtoWithNullFieldsTest() {
        CreateCourierDto dto = new CreateCourierDto(
            null,
            null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void createCourierDtoWithEmptyFieldsTest() {
        CreateCourierDto dto = new CreateCourierDto(
            "",
            "");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void createCourierDtoWithInvalidFieldsTest() {
        CreateCourierDto dto = new CreateCourierDto(
            "@#$",
            "@#$");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void createCourierDtoWithInvalidLanguageOfFieldsTest() {
        CreateCourierDto dto = new CreateCourierDto(
            "Тест",
            "Test");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void createCourierDtoWithInvalidLengthOfFieldsTest() {
        CreateCourierDto dto = new CreateCourierDto(
            "Test111111111111111111111111111",
            "Тест111111111111111111111111111");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateCourierDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }
}
