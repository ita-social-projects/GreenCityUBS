package greencity.dto.courier;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddingReceivingStationDtoTest {
    void nameRegex(String name, boolean validates) throws NoSuchFieldException {
        Field field = AddingReceivingStationDto.class.getDeclaredField("name");
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
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

    @Test
    void testNameWithEnglishLetterInBothCases() throws NoSuchFieldException {
        nameRegex("qweQWE", true);
    }

    @Test
    void testNameWithUkrainianLetterInBothCases() throws NoSuchFieldException {
        nameRegex("абвгґіїьяюєАБВГҐІЇЬЯЮЄ", true);
    }

    @Test
    void testNameWithNumbers() throws NoSuchFieldException {
        nameRegex("1234567890", true);
    }

    @Test
    void testNameWithHyphen() throws NoSuchFieldException {
        nameRegex("qwe-qwe", true);
    }

    @Test
    void testNameWithDot() throws NoSuchFieldException {
        nameRegex("qwe.qwe", true);
    }

    @Test
    void testNameWithWhitespace() throws NoSuchFieldException {
        nameRegex("qwe qwe", true);
    }

    @Test
    void testNameWithApostrophe() throws NoSuchFieldException {
        nameRegex("qwe'qwe", true);
    }
}