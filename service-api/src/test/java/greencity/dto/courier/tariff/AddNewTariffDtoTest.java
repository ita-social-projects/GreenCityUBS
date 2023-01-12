package greencity.dto.courier.tariff;

import greencity.ModelUtils;
import greencity.dto.AddNewTariffDto;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

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

    @Test
    void testAddNewTariffDtoWithInvalidRegionIdFieldTest() {
        AddNewTariffDto dto = new AddNewTariffDto(
                null,
                null,
                Collections.emptyList(),
                null);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddNewTariffDto>> constraintViolations =
                validator.validate(dto);

        assertThat(constraintViolations.size()).isEqualTo(4);
    }
}
