package greencity.dto.employee;

import greencity.ModelUtils;
import greencity.dto.position.PositionDto;
import greencity.dto.tariff.TariffWithChatAccess;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
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
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String validName = "Valid";
    private static final String validEmail = "mail@gmail.com";
    private static final String validPhoneNumber = "+390990000000";
    private static final List<PositionDto> validPositions = List.of(ModelUtils.getEmployeePosition());
    private static final long validId = 1L;
    private static final List<Long> validIds = List.of(1L);

    @ParameterizedTest
    @MethodSource("provideValidNamePairs")
    void validNamesInEmployeeDtoTest(String firstName, String lastName) {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                firstName, lastName, validEmail,
                validPhoneNumber, validPositions,
                validId, validIds, true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidNamePairs")
    void invalidNamesInEmployeeDtoTest(String firstName, String lastName) {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                firstName, lastName, validEmail,
                validPhoneNumber, validPositions,
                validId, validIds, true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSizeBetween(1, 2);
    }

    @ParameterizedTest
    @MethodSource("provideValidEmails")
    void validEmailInEmployeeDtoTest(String email) {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                validName, validName, email,
                validPhoneNumber, validPositions,
                validId, validIds, true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidEmails")
    void invalidEmailInEmployeeDtoTest(String email) {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                validName, validName, email,
                validPhoneNumber, validPositions,
                validId, validIds, true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(1);
    }

    @Test
    void validFieldsInEmployeeDtoTest() {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                validName, validName, validEmail,
                validPhoneNumber, validPositions,
                validId, validIds, true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @Test
    void invalidFieldsInEmployeeDtoTest() {
        EmployeeWithTariffsIdDto dto =
            createEmployeeWithTariffsDto(
                "", "", "",
                "", null,
                -1L, List.of(), true);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(5);
    }

    private static Stream<Arguments> provideValidNamePairs() {
        return Stream.of(
            Arguments.of("F", "L"),
            Arguments.of("FirstName", "LastName"),
            Arguments.of("firstName", "lastName"),
            Arguments.of("First-Name", "Last-Name"),
            Arguments.of("Лук'ян", "Їгор"),
            Arguments.of("Петро1", "ІЄгор1"),
            Arguments.of("Лук'ян-", "Єгор-"),
            Arguments.of("Лук'ян ", "Єгор "),
            Arguments.of("Лук' ян", "Є гор"),
            Arguments.of("Лук'ян.н", "Єгор.р"),
            Arguments.of("Петро", "Ґгор"),
            Arguments.of("лук'ян", "їєґгор"));
    }

    private static Stream<Arguments> provideInvalidNamePairs() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("Лук'ян+", "Єгор+"),
            Arguments.of("+Лук'ян", "+Єгор"),
            Arguments.of("-Лук'ян", "-Єгор"),
            Arguments.of("1Лук'ян", "2Єгор"),
            Arguments.of("Лук..ян", "Є..гор"),
            Arguments.of(null, null),
            Arguments.of(" ", " "),
            Arguments.of(".", "."),
            Arguments.of("T.", "T."),
            Arguments.of("T..", "T.."),
            Arguments.of("T...", "T..."),
            Arguments.of("T--", "T--"),
            Arguments.of("T---", "T---"),
            Arguments.of("''", "''"),
            Arguments.of("Ttttttttttttttttttttttttttttttt", "Ttttttttttttttttttttttttttttttt"));
    }

    private static Stream<Arguments> provideValidEmails() {
        return Stream.of(
            Arguments.of("mail@gmail.com"),
            Arguments.of("mail@gmail.org"),
            Arguments.of("Mail@gmail.com"),
            Arguments.of("mail_@gmail.com"),
            Arguments.of("Mail21_@gmail.com"),
            Arguments.of("mail21@gmail.com"),
            Arguments.of("mail@somemail.com"),
            Arguments.of("1mail@somemail.com"),
            Arguments.of("mail+1@gmail.com"));
    }

    private static Stream<Arguments> provideInvalidEmails() {
        return Stream.of(
            Arguments.of("mail.@gmail.com"),
            Arguments.of(".mail@gmail.org"),
            Arguments.of("gmail.com"),
            Arguments.of("@gmail.com"),
            Arguments.of("mail@gmail"),
            Arguments.of("mail@gmailcom"),
            Arguments.of("ma!il@gmail.com"));
    }

    private static EmployeeWithTariffsIdDto createEmployeeWithTariffsDto(
        String firstName, String lastName, String email,
        String phoneNumber, List<PositionDto> employeePositions, Long employeeId,
        List<Long> tariffIds, boolean hasChat) {
        return EmployeeWithTariffsIdDto.builder()
            .employeeDto(EmployeeDto.builder()
                .id(employeeId)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .email(email)
                .employeePositions(employeePositions)
                .build())
            .tariffs(createTariffsWithChatAccess(tariffIds, hasChat))
            .build();
    }

    private static List<TariffWithChatAccess> createTariffsWithChatAccess(
        List<Long> tariffIds, boolean hasChat) {
        return tariffIds.stream()
            .map(id -> TariffWithChatAccess.builder()
                .tariffId(id)
                .hasChat(hasChat)
                .build())
            .toList();
    }
}
