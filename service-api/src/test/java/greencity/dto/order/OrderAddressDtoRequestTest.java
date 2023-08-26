package greencity.dto.order;

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

class OrderAddressDtoRequestTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validFieldsInAddressDtoTest(String street) {
        var dto = OrderAddressDtoRequest.builder()
            .region("region")
            .regionEn("regionEn")
            .district("district")
            .districtEn("districtEn")
            .houseNumber("1")
            .city("city")
            .cityEn("cityEn")
            .street(street)
            .streetEn(street)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<OrderAddressDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidFieldsInAddressDtoTest(String street) {
        var dto = OrderAddressDtoRequest.builder()
            .region("region")
            .regionEn("regionEn")
            .district("district")
            .districtEn("districtEn")
            .houseNumber("1")
            .city("city")
            .cityEn("cityEn")
            .street(street)
            .streetEn(street)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<OrderAddressDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of("Shevchenka"),
            Arguments.of("Шевченка"),
            Arguments.of("Shevchenka-Khreschatyk"),
            Arguments.of("Шевченка-Хрещатик"),
            Arguments.of("Street-Вулиця"),
            Arguments.of("Sviatoshins'ka"),
            Arguments.of("Святошиньска"),
            Arguments.of("Незалежності"),
            Arguments.of("їҐґЄє"),
            Arguments.of("1-ho Travnya"),
            Arguments.of("Protasiv Yar"),
            Arguments.of("Протасів Яр"));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of("Shevchenka+1"),
            Arguments.of("~Шевченка"),
            Arguments.of("123"),
            Arguments.of("!Хрещатик2"));
    }
}
