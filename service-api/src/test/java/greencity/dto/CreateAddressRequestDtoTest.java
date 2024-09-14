package greencity.dto;

import greencity.dto.location.CoordinatesDto;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;
import static greencity.ModelUtils.createDefaultAddress;
import static greencity.ModelUtils.createDifferentAddress;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CreateAddressRequestDtoTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validFieldsInAddressDtoTest(String street) {
        var dto = CreateAddressRequestDto.builder()
            .region("region")
            .regionEn("regionEn")
            .district("district")
            .districtEn("districtEn")
            .houseNumber("1")
            .placeId("test")
            .coordinates(CoordinatesDto.builder()
                .latitude(1D)
                .longitude(1D)
                .build())
            .city("city")
            .cityEn("cityEn")
            .street(street)
            .streetEn(street)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateAddressRequestDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).isEmpty();
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidFieldsInAddressDtoTest(String street) {
        var dto = CreateAddressRequestDto.builder()
            .region("region")
            .regionEn("regionEn")
            .district("district")
            .districtEn("districtEn")
            .houseNumber("1")
            .placeId("test")
            .coordinates(CoordinatesDto.builder()
                .latitude(1D)
                .longitude(1D)
                .build())
            .city("city")
            .cityEn("cityEn")
            .street(street)
            .streetEn(street)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CreateAddressRequestDto>> constraintViolations =
            validator.validate(dto);

        assertThat(constraintViolations).hasSize(2);
    }

    @Test
    void testEquals_SameObject_ShouldReturnTrue() {
        CreateAddressRequestDto address1 = createDefaultAddress();

        assertThat(address1.equals(address1)).isTrue();
    }

    @Test
    void testEquals_DifferentObjectWithSameValues_ShouldReturnTrue() {
        CreateAddressRequestDto address1 = createDefaultAddress();
        CreateAddressRequestDto address2 = createDefaultAddress();

        assertThat(address1.equals(address2)).isTrue();
    }

    @Test
    void testEquals_DifferentObjectWithDifferentValues_ShouldReturnFalse() {
        CreateAddressRequestDto address1 = createDefaultAddress();
        CreateAddressRequestDto address2 = createDifferentAddress();

        assertThat(address1.equals(address2)).isFalse();
    }

    @Test
    void testHashCode_SameObject_ShouldReturnSameHashCode() {
        CreateAddressRequestDto address1 = createDefaultAddress();

        assertEquals(address1.hashCode(), address1.hashCode());
    }

    @Test
    void testHashCode_DifferentObjectWithSameValues_ShouldReturnSameHashCode() {
        CreateAddressRequestDto address1 = createDefaultAddress();
        CreateAddressRequestDto address2 = createDefaultAddress();

        assertEquals(address1.hashCode(), address2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjectWithDifferentValues_ShouldReturnDifferentHashCode() {
        CreateAddressRequestDto address1 = createDefaultAddress();
        CreateAddressRequestDto address2 = createDifferentAddress();

        assertNotEquals(address1.hashCode(), address2.hashCode());
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
