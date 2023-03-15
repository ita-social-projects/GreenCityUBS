package greencity.dto.courier.employee;

import greencity.ModelUtils;
import greencity.dto.employee.EmployeeDto;
import greencity.dto.employee.EmployeeWithTariffsIdDto;
import greencity.dto.position.PositionDto;
import lombok.SneakyThrows;
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

class EmployeeWithTariffsIdDtoTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validFieldsInEmployeeDtoTest(Long id,
        String firstName,
        String lastName,
        String email,
        List<PositionDto> employeePositions) {

        var dto = EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+390990000000")
                .email(email)
                .employeePositions(employeePositions)
                .build())
            .tariffId(List.of(1L))
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidFieldsInEmployeeDtoTest(Long id,
        String firstName,
        String lastName,
        String email,
        List<PositionDto> employeePositions) {

        var dto = EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+390990000000")
                .email(email)
                .employeePositions(employeePositions)
                .build())
            .tariffId(List.of(1L))
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(5);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of(1L, "FirstName", "LastName", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(2L, "Лук'ян", "Їгор", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(3L, "Лук'ян", "Ігор", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(4L, "Лук'ян", "Єгор", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(5L, "Лук'ян1", "Єгор1", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(6L, "Лук'ян+", "Єгор+", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(7L, "Лук'ян-", "Єгор-", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(8L, "Лук'ян ", "Єгор ", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(9L, "Лук'ян.н", "Єгор.р", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(10L, "Лук'ян", "Ґгор", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())),
            Arguments.of(11L, "лук'ян", "ґгор", "mail@gmail.com", List.of(ModelUtils.getEmployeePosition())));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of(0L, "", "", "", null),
            Arguments.of(-1L, null, null, "", null),
            Arguments.of(-2L, null, null, null, null),
            Arguments.of(-3L, " ", " ", "", null),
            Arguments.of(-4L, ".", ".", ".", null),
            Arguments.of(-5L, "T.", "T.", "T.", null),
            Arguments.of(-6L, "T..", "T..", "T..", null),
            Arguments.of(-7L, "T...", "T...", "T...", null),
            Arguments.of(-8L, "T--", "T--", "T--", null),
            Arguments.of(-9L, "T---", "T---", "T---", null),
            Arguments.of(-10L, "''", "''", "''", null),
            Arguments.of(-11L, "Ttttttttttttttttttttttttttttttt", "Ttttttttttttttttttttttttttttttt", "", null));
    }
}
