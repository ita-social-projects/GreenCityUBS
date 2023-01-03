package greencity.dto.courier;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceivingStationDtoTest {
    void nameRegex(String name, boolean validates) throws NoSuchFieldException {
        Field field = ReceivingStationDto.class.getDeclaredField("name");
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"qweQWE", "абвгґіїьяюєАБВГҐІЇЬЯЮЄ", "1234567890", "qwe-qwe", "qwe.qwe", "qwe qwe", "qwe'qwe"})
    void testNameRegex(String name) throws NoSuchFieldException {
        nameRegex(name, true);
    }

    @Test
    void testEmptyName() throws NoSuchFieldException {
        nameRegex("", false);
    }

    @Test
    void testNameWithMinimumCountOfSymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 1);
        nameRegex(name, true);
    }

    @Test
    void testNameWithMaximumCountOfSymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 30);
        nameRegex(name, true);
    }

    @Test
    void testNameWithTooManySymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 31);
        nameRegex(name, false);
    }
}