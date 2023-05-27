package greencity.dto.courier.tariff;

import greencity.ModelUtils;
import greencity.dto.AddNewTariffDto;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    void addNewTariffDtoWithNullAndEmptyFieldsTest() {
        AddNewTariffDto dto = new AddNewTariffDto(
            null,
            null,
            Collections.emptyList(),
            null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void addNewTariffDtoWithInvalidValuesTest() {
        AddNewTariffDto dto = new AddNewTariffDto(
            0L,
            0L,
            List.of(0L),
            List.of(0L));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(4);
    }
}
