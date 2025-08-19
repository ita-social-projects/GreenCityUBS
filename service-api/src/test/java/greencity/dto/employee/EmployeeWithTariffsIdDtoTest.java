package greencity.dto.employee;

import greencity.dto.tariff.TariffWithChatAccess;
import greencity.entity.user.employee.Position;
import greencity.repository.PositionRepository;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = EmployeeWithTariffsIdDtoTest.TestConfig.class)
class EmployeeWithTariffsIdDtoTest {
    @Autowired
    Validator validator;

    @MockBean
    private PositionRepository positionRepository;

    private static final String validName = "Valid";
    private static final String validEmail = "mail@gmail.com";
    private static final String validPhoneNumber = "+380938754569";
    private static final Set<Long> validPositionIds = Set.of(1L, 2L, 3L);
    private static final long validId = 1L;
    private static final List<Long> validIds = List.of(1L);

    @TestConfiguration
    static class TestConfig {
        @Bean
        public Validator validatorFactory() {
            return new LocalValidatorFactoryBean();
        }
    }

    @BeforeEach
    void setUp() {
        when(positionRepository.findAllById(validPositionIds))
                .thenReturn(validPositionIds.stream()
                        .map(id -> {
                            Position pos = new Position();
                            pos.setId(id);
                            return pos;
                        })
                        .toList());

        when(positionRepository.findByIdIn(validPositionIds))
                .thenReturn(validPositionIds.stream()
                        .map(id -> {
                            Position pos = new Position();
                            pos.setId(id);
                            return pos;
                        })
                        .collect(Collectors.toSet()));
    }

    @Nested
    @DisplayName("Name validation")
    class NameValidation {

        @ParameterizedTest
        @MethodSource("provideValidNamePairs")
        void shouldAcceptValidNames(String firstName, String lastName) {
            EmployeeWithTariffsIdDto dto =
                createEmployeeWithTariffsDto(firstName, lastName, validEmail, validPhoneNumber);
            assertThat(validator.validate(dto)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("provideInvalidNamePairs")
        void shouldRejectInvalidNames(String firstName, String lastName) {
            EmployeeWithTariffsIdDto dto =
                createEmployeeWithTariffsDto(firstName, lastName, validEmail, validPhoneNumber);
            assertThat(validator.validate(dto)).isNotEmpty();
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
                Arguments.of("лук'ян", "їєґгор"),
                Arguments.of("Іван-Петро", "Кирило-Миколайович"),
                Arguments.of("Dr.Ігор", "П.Іванович"),
                Arguments.of("Тест-Test", "Прізвище-Family"),
                Arguments.of("Євген’О’Браєн", "Ґудзик"));
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
                Arguments.of("Ttttttttttttttttttttttttttttttt", "Ttttttttttttttttttttttttttttttt"),
                Arguments.of("A - - B", "C - - D"),
                Arguments.of("І. .ван", "Є. .гор"),
                Arguments.of("Є ’     ’ ван", "Ґ ’ ’"),
                Arguments.of("Test '' Name", "Last '' Name"));
        }
    }

    @Nested
    @DisplayName("Email validation")
    class EmailValidation {

        @ParameterizedTest
        @MethodSource("provideValidEmails")
        void shouldAcceptValidEmails(String email) {
            EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, email, validPhoneNumber);
            assertThat(validator.validate(dto)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("provideInvalidEmails")
        void shouldRejectInvalidEmails(String email) {
            EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, email, validPhoneNumber);
            assertThat(validator.validate(dto)).hasSize(1);
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
                Arguments.of("mail@gmailcom"));
        }
    }

    @Nested
    @DisplayName("Phone number validation")
    class PhoneNumberValidation {

        @ParameterizedTest
        @MethodSource("validPhoneNumbers")
        void shouldAcceptValidPhoneNumbers(String phone) {
            EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, validEmail, phone);
            assertThat(validator.validate(dto)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("invalidPhoneNumbers")
        void shouldRejectInvalidPhoneNumbers(String phone) {
            EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, validEmail, phone);
            assertThat(validator.validate(dto)).isNotEmpty();
        }

        @Test
        void shouldRejectNullPhoneNumber() {
            EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, validEmail, null);
            Set<ConstraintViolation<EmployeeWithTariffsIdDto>> violations = validator.validate(dto);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .contains("employeeDto.phoneNumber");
        }

        private static Stream<Arguments> validPhoneNumbers() {
            return Stream.of(
                Arguments.of("+380938754569"),
                Arguments.of("+38(093)87-54-569"),
                Arguments.of("380998754569"),
                Arguments.of("0678754569"),
                Arguments.of("938754569"));
        }

        private static Stream<Arguments> invalidPhoneNumbers() {
            return Stream.of(
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("text"),
                Arguments.of("067875Dhgjh4569"),
                Arguments.of("0114860406"),
                Arguments.of("4860406"),
                Arguments.of("+"));
        }
    }

    @Test
    void shouldBeValidWithAllValidFields() {
        EmployeeWithTariffsIdDto dto = createEmployeeWithTariffsDto(validName, validName, validEmail, validPhoneNumber);
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void shouldBeInvalidWithEmptyOrMissingFields() {
        EmployeeWithTariffsIdDto dto = new EmployeeWithTariffsIdDto();
        dto.setEmployeeDto(new CreateUpdateEmployeeDto());
        dto.setTariffs(null);

        Set<ConstraintViolation<EmployeeWithTariffsIdDto>> violations = validator.validate(dto);

        assertThat(violations).hasSize(6);

        Set<String> fieldsWithViolations = violations.stream()
            .map(v -> v.getPropertyPath().toString())
            .collect(Collectors.toSet());

        assertThat(fieldsWithViolations).containsExactlyInAnyOrder(
            "employeeDto.email",
            "employeeDto.employeePositionIds",
            "employeeDto.firstName",
            "employeeDto.lastName",
            "employeeDto.phoneNumber",
            "tariffs");
    }

    private static EmployeeWithTariffsIdDto createEmployeeWithTariffsDto(
        String firstName, String lastName, String email, String phoneNumber) {

        return EmployeeWithTariffsIdDto.builder()
            .employeeDto(CreateUpdateEmployeeDto.builder()
                .id(validId)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .email(email)
                .employeePositionIds(validPositionIds)
                .build())
            .tariffs(createTariffsWithChatAccess())
            .build();
    }

    private static List<TariffWithChatAccess> createTariffsWithChatAccess() {
        return EmployeeWithTariffsIdDtoTest.validIds.stream()
            .map(id -> TariffWithChatAccess.builder()
                .tariffId(id)
                .hasChat(true)
                .build())
            .toList();
    }
}
