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
    void validFieldsInAddressDtoTest(String houseNumber, String cityEn) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .cityEn(cityEn)
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
    void invalidFieldsInAddressDtoTest(String houseNumber, String cityEn) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .cityEn(cityEn)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddressDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of("1", "Kharkiv"),
            Arguments.of("1F", "Kyiv"),
            Arguments.of("1B", "Pryp'yat'"),
            Arguments.of("1Ї", "Kam'yanets Podilskyi"),
            Arguments.of("1-А", "Korsun Shevchenkivskiy"),
            Arguments.of("1.Б", "Bila Krynytsya"),
            Arguments.of("1 G", "Kam'yanets Podilskyi"),
            Arguments.of("ЁёІіЇ", "Bilgorod-Dnistrovskyi"),
            Arguments.of("їҐґЄє", "Blagovishchens'k"),
            Arguments.of("1/3", "Ternopil'"),
            Arguments.of("35/34", "Vinnitsa"),
            Arguments.of("35-/34", "Vilnohirs'k"),
            Arguments.of("35-/ 34", "Rivne"),
            Arguments.of("35-/\"34", "Pereyaslav"),
            Arguments.of("14\"o\"", "Zytomyr"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of("", "0Kharkiv"),
            Arguments.of("@#$", "kyiv"),
            Arguments.of("Testtttttttt", " kharkiv"),
            Arguments.of("Тесттттттттт", "-Rivne"));
    }
}
