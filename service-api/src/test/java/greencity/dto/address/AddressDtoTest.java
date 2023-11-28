package greencity.dto.address;

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
    void validFieldsInAddressDtoTest(String houseNumber, String cityEn, String street) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .cityEn(cityEn)
            .street(street)
            .streetEn(street)
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
    void invalidFieldsInAddressDtoTest(String houseNumber, String cityEn, String street) {
        var dto = AddressDto.builder()
            .id(1L)
            .houseNumber(houseNumber)
            .cityEn(cityEn)
            .street(street)
            .streetEn(street)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<AddressDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(4);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of("1", "Kharkiv", "Shevchenka"),
            Arguments.of("1F", "Kyiv", "Шевченка"),
            Arguments.of("1B", "Pryp'yat'", "Khreschatyk"),
            Arguments.of("1Ї", "Kam'yanets Podilskyi", "Хрещатик"),
            Arguments.of("1-А", "Korsun Shevchenkivskiy", "Shevchenka-Khreschatyk"),
            Arguments.of("1.Б", "Bila Krynytsya", "Шевченка-Хрещатик"),
            Arguments.of("1 G", "Kam'yanets Podilskyi", "Street"),
            Arguments.of("ЁёІіЇ", "Bilgorod-Dnistrovskyi", "Вулиця"),
            Arguments.of("їҐґЄє", "Blagovishchens'k", "Street-Вулиця"),
            Arguments.of("1/3", "Ternopil'", "Sviatoshins'ka"),
            Arguments.of("35/34", "Vinnitsa", "Святошиньска"),
            Arguments.of("35-/34", "Vilnohirs'k", "Незалежності"),
            Arguments.of("35-/ 34", "Rivne", "1-ho Travnya"),
            Arguments.of("35-/\"34", "Pereyaslav", "Protasiv Yar"),
            Arguments.of("14\"o\"", "Zytomyr", "Протасів Яр"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of("", "0Kharkiv", "Shevchenka+1"),
            Arguments.of("@#$", "kyiv", "~Шевченка"),
            Arguments.of("Testtttttttt", " kharkiv", "+шевченка"),
            Arguments.of("Тесттттттттт", "-Rivne", "1234"));
    }
}
