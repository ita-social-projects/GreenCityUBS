package greencity.dto.courier.address;

import greencity.dto.address.AddressDto;
import lombok.SneakyThrows;
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

class AddressDtoTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validHouseNumbersInAddressDtoTest(String houseNumber) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddressDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidHouseNumbersInAddressDtoTest(String houseNumber) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddressDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(1);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of("1"),
            Arguments.of("1F"),
            Arguments.of("1B"),
            Arguments.of("1Ї"),
            Arguments.of("1-А"),
            Arguments.of("1.Б"),
            Arguments.of("1 G"),
            Arguments.of("ЁёІіЇ"),
            Arguments.of("їҐґЄє"),
            Arguments.of("1/3"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("@#$"),
            Arguments.of("Testtt"),
            Arguments.of("Тесттт"));
    }
}
