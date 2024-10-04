package greencity.dto.employee;

import greencity.ModelUtils;
import greencity.dto.tariff.TariffWithChatAccess;
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
            .tariffs(List.of(TariffWithChatAccess.builder()
                .tariffId(1L)
                .hasChat(true)
                .build()))
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
            .tariffs(List.of())
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(5);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of("Олександр", "Петро"),
            Arguments.of("Іван", "Єгор"),
            Arguments.of("Лук'ян", "Їгор"),
            Arguments.of("Петро", "І Єгор"),
            Arguments.of("Лук'ян+", "Єгор"),
            Arguments.of("Лук'ян-", "Єгор"),
            Arguments.of("Лук'ян ", "Єгор"),
            Arguments.of("Лук'ян.н", "Єгор.р"),
            Arguments.of("Петро", "Ґгор"),
            Arguments.of("лук'ян", "їєґгор"),
            Arguments.of("Олександра", "Петрівна"),
            Arguments.of("Іванна", "Єгорівна"),
            Arguments.of("Лук'яна", "Їгорівна"),
            Arguments.of("Петра", "І Єгорівна"),
            Arguments.of("Лук'яна+", "Єгорівна"),
            Arguments.of("Лук'яна-", "Єгорівна"),
            Arguments.of("Лук'яна ", "Єгорівна"),
            Arguments.of("Ivan", "ABC.р"),
            Arguments.of("Ivan", "Ivanov"),
            Arguments.of("Ivan", "Doe"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of("ъван", "ъегор", "mail.com"),
            Arguments.of("ыван", "ыегор", "mail.com"),
            Arguments.of("ёван", "ёегор", "mail.com"),
            Arguments.of("эван", "эегор", "mail.com"),
            Arguments.of("ъванъ", "ъегоръ", "mail.com"),
            Arguments.of("ываны", "ыегоры", "mail.com"),
            Arguments.of("ёванё", "ёегорё", "mail.com"),
            Arguments.of("эванэ", "эегорэ", "mail.com"),
            Arguments.of("ъванъы", "ъегоръы", "mail.com"),
            Arguments.of("ываныё", "ыегорыё", "mail.com"),
            Arguments.of("ёванёэ", "ёегорёэ", "mail.com"),
            Arguments.of("эванэъ", "эегорэъ", "mail.com"),
            Arguments.of(null , null, "invalid_email"),
            Arguments.of("Олексій--", "ъгорович", "mail@.com"),
            Arguments.of("І.в.ан..", "!Іван", "mail.com@"),
            Arguments.of("Іван--Івано", "..", "mail..com"),
            Arguments.of("Коп''ютер", "@Петрович", "mail.com."),
            Arguments.of("", "", "mail@com"),
            Arguments.of("1234ван", "і!!", "mail@.com.au"),
            Arguments.of(null, null, null),
            Arguments.of("iv!an", "Иван''", "mail@.au"));
    }
}
