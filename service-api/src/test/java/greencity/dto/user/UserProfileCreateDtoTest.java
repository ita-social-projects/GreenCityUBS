package greencity.dto.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileCreateDtoTest {

    private final String mockName = "Maksym";
    private final String mockUuid = "test uuid";
    private final String mockEmail = "test@gmail.com";

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideValidEmails")
    void validEmailInUserProfileCreateDtoTest(String email) {
        UserProfileCreateDto userProfileCreateDto = UserProfileCreateDto.builder()
            .uuid(mockUuid)
            .name(mockName)
            .email(email)
            .build();

        validate(userProfileCreateDto, 0);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideInvalidEmails")
    void invalidEmailInUserProfileCreateDtoTest(String email) {
        UserProfileCreateDto userProfileCreateDto = UserProfileCreateDto.builder()
            .uuid(mockUuid)
            .name(mockName)
            .email(email)
            .build();

        validate(userProfileCreateDto, 1);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideValidUsernames")
    void validNameInUserProfileCreateDtoTest(String name) {
        UserProfileCreateDto userProfileCreateDto = UserProfileCreateDto.builder()
            .uuid(mockUuid)
            .name(name)
            .email(mockEmail)
            .build();

        validate(userProfileCreateDto, 0);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideInvalidUsernames")
    void invalidNameInUserProfileCreateDtoTest(String name) {
        UserProfileCreateDto userProfileCreateDto = UserProfileCreateDto.builder()
            .uuid(mockUuid)
            .name(name)
            .email(mockEmail)
            .build();

        validate(userProfileCreateDto, 1);
    }

    private void validate(UserProfileCreateDto userProfileCreateDto, int constraintViolationsSize) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            final Validator validator = factory.getValidator();

            Set<ConstraintViolation<UserProfileCreateDto>> constraintViolations =
                validator.validate(userProfileCreateDto);

            assertThat(constraintViolations).hasSize(constraintViolationsSize);
        }
    }

    private static Stream<Arguments> provideValidEmails() {
        return Stream.of(
            Arguments.of("example@example.com"),
            Arguments.of("user.name+tag+sorting@example.com"),
            Arguments.of("user_name@example.co.uk"),
            Arguments.of("user-name123@example.io"),
            Arguments.of("u@example.me"));
    }

    private static Stream<Arguments> provideInvalidEmails() {
        return Stream.of(
            Arguments.of("plainaddress"),
            Arguments.of("@missingusername.com"),
            Arguments.of("username@.com"),
            Arguments.of("username@com"), // missing dot-suffix
            Arguments.of("username@domain.c"), // TLD too short
            Arguments.of("username@domain.toolongtld"), // TLD too long (>6)
            Arguments.of("user@@example.com"),
            Arguments.of("user@ example.com"), // space in domain
            Arguments.of("user@example..com") // double dots
        );
    }

    private static Stream<Arguments> provideValidUsernames() {
        return Stream.of(
            Arguments.of("T"),
            Arguments.of("Tt"),
            Arguments.of("T.t"),
            Arguments.of("T-"),
            Arguments.of("T'"),
            Arguments.of("T'’"),
            Arguments.of("T'’.t"),
            Arguments.of("T2"),
            Arguments.of("ІіЇїҐґЄє"),
            Arguments.of("Імʼя"),
            Arguments.of("Тест"),
            Arguments.of("Тест123"),
            Arguments.of("Ґ.Ї.Є"),
            Arguments.of("Тест-Тест"),
            Arguments.of("ІмʼяʼТест"),
            Arguments.of("АртемʼЄвгенович"),
            Arguments.of("Test"),
            Arguments.of("Test.Name"),
            Arguments.of("Test-Name"),
            Arguments.of("Name'Last"),
            Arguments.of("Name2"),
            Arguments.of("Імʼя.Тест"),
            Arguments.of("ҐрімʼТест"),
            Arguments.of("ЄвгенҐ123"),
            Arguments.of("Ім'я-Тест"),
            Arguments.of("Test-Test"),
            Arguments.of("User123"),
            Arguments.of("Імʼя2Тест"),
            Arguments.of("ТестʼІмʼя"),
            Arguments.of("Євген.Тест"),
            Arguments.of("lowercase-name"));
    }

    private static Stream<Arguments> provideInvalidUsernames() {
        return Stream.of(
            Arguments.of("."),
            Arguments.of(".."),
            Arguments.of("-"),
            Arguments.of("'"),
            Arguments.of("ʼ"),
            Arguments.of("ʼʼ"),
            Arguments.of("--"),
            Arguments.of("..Тест"),
            Arguments.of("Тест.."),
            Arguments.of("тест.."),
            Arguments.of("Тест..Імʼя"),
            Arguments.of("Тест--Імʼя"),
            Arguments.of("Тест.."),
            Arguments.of("Тест..Тест"),
            Arguments.of("Тест--Тест"),
            Arguments.of("Тест@Імʼя"),
            Arguments.of("Імʼя#Тест"),
            Arguments.of("Імʼя_Тест!"),
            Arguments.of("ІмʼяТестЗанадтоДовгеБільше30Символів"),
            Arguments.of("12345"),
            Arguments.of(" Імʼя"),
            Arguments.of(".."),
            Arguments.of("1234567890123456789012345678901"),
            Arguments.of(""),
            Arguments.of(" "),
            Arguments.of("T.."),
            Arguments.of("T--"),
            Arguments.of("T."),
            Arguments.of("1test"),
            Arguments.of("@#$"),
            Arguments.of("Testttttttttttttttttttttttttttt"));
    }
}
