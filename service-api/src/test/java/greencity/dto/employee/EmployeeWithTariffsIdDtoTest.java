package greencity.dto.employee;

import greencity.ModelUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

class EmployeeWithTariffsIdDtoTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validFieldsInEmployeeDtoTest(String firstName, String lastName) {

        var dto = EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(1L)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+390990000000")
                .email("mail@gmail.com")
                .employeePositions(List.of(ModelUtils.getEmployeePosition()))
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
    void invalidFieldsInEmployeeDtoTest(String firstName, String lastName, String email) {

        var dto = EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(-1L)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber("+390990000000")
                .email(email)
                .employeePositions(null)
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
            Arguments.of("FirstName", "LastName"),
            Arguments.of("firstName", "lastName"),
            Arguments.of("Лук'ян", "Їгор"),
            Arguments.of("Петро1", "ІЄгор1"),
            Arguments.of("Лук'ян+", "Єгор+"),
            Arguments.of("Лук'ян-", "Єгор-"),
            Arguments.of("Лук'ян ", "Єгор "),
            Arguments.of("Лук'ян.н", "Єгор.р"),
            Arguments.of("Петро", "Ґгор"),
            Arguments.of("лук'ян", "їєґгор"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of("", "", ""),
            Arguments.of(null, null, "mail.com"),
            Arguments.of(null, null, null),
            Arguments.of(" ", " ", ""),
            Arguments.of(".", ".", "."),
            Arguments.of("T.", "T.", "T."),
            Arguments.of("T..", "T..", "T.."),
            Arguments.of("T...", "T...", "T..."),
            Arguments.of("T--", "T--", "T--"),
            Arguments.of("T---", "T---", "T---"),
            Arguments.of("''", "''", "''"),
            Arguments.of("Ttttttttttttttttttttttttttttttt", "Ttttttttttttttttttttttttttttttt", "mail@"));
    }
}
