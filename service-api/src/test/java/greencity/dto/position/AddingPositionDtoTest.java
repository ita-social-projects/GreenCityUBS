package greencity.dto.position;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddingPositionDtoTest {
    void nameRegex(String name, boolean validates) throws NoSuchFieldException {
        Field field = AddingPositionDto.class.getDeclaredField("name");
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"John Doe", "абвгґіїьяюєАБВГҐІЇЬЯЮЄ", "John-Doe", "John Doe", "John'Doe",
            "SymbolsIsValidNameLength"})
    void testValidName(String name) throws NoSuchFieldException {
        nameRegex(name, true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"", "31SymbolsIsInvalidNameLengthForAddingPosition"})
    void testInvalidName(String name) throws NoSuchFieldException {
        nameRegex(name, false);
    }

    void nameEnRegex(String nameEn, boolean validates) throws NoSuchFieldException {
        Field field = AddingPositionDto.class.getDeclaredField("nameEn");
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(nameEn.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"John Doe", "John-Doe", "John Doe", "John'Doe",
            "SymbolsIsValidNameLengthForAdd"})
    void testValidNameEn(String nameEn) throws NoSuchFieldException {
        nameEnRegex(nameEn, true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"", "31SymbolsIsInvalidNameLengthForAddingPosition"})
    void testInvalidNameEn(String nameEn) throws NoSuchFieldException {
        nameEnRegex(nameEn, false);
    }
}