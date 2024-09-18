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

    @Test
    void testEqualsForAllFields() {
        CreateAddressRequestDto dto1 = createDefaultAddress();
        CreateAddressRequestDto dto2 = createDefaultAddress();

        assertThat(dto1).isEqualTo(dto2);

        dto2.setDistrictEn("Different District En");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setDistrictEn(dto1.getDistrictEn());

        dto2.setDistrict("Different District");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setDistrict(dto1.getDistrict());

        dto2.setRegionEn("Different Region En");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setRegionEn(dto1.getRegionEn());

        dto2.setRegion("Different Region");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setRegion(dto1.getRegion());

        dto2.setHouseNumber("999");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setHouseNumber(dto1.getHouseNumber());

        dto2.setEntranceNumber("Different Entrance Number");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setEntranceNumber(dto1.getEntranceNumber());

        dto2.setHouseCorpus("Different Corpus");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setHouseCorpus(dto1.getHouseCorpus());

        dto2.setCoordinates(new CoordinatesDto(48.8584, 2.2945));
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setCoordinates(dto1.getCoordinates());

        dto2.setCity("Different City");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setCity(dto1.getCity());

        dto2.setCityEn("Different City En");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setCityEn(dto1.getCityEn());

        dto2.setStreet("Different Street");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setStreet(dto1.getStreet());

        dto2.setStreetEn("Different Street En");
        assertThat(dto1).isNotEqualTo(dto2);
        dto2.setStreetEn(dto1.getStreetEn());
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
