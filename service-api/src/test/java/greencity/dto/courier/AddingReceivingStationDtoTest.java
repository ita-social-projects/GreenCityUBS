package greencity.dto.courier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AddingReceivingStationDtoTest {
    void nameRegex(String name, boolean validates) throws NoSuchFieldException {
        Field field = AddingReceivingStationDto.class.getDeclaredField("name");
        jakarta.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(jakarta.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"qweQWE", "абвгґіїьяюєАБВГҐІЇЬЯЮЄ", "1234567890", "qwe-qwe", "qwe qwe", "qwe'qwe",
            "40 SymbolsIsValidNameLengthForNewStation"})
    void testValidName(String name) throws NoSuchFieldException {
        nameRegex(name, true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"", "a", "qwe.qwe", "41SymbolsIsInvalidNameLengthForNewStation"})
    void testInvalidName(String name) throws NoSuchFieldException {
        nameRegex(name, false);
    }

}