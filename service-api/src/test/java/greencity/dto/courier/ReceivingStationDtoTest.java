package greencity.dto.courier;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceivingStationDtoTest {
    public void nameRegex(String name, boolean validates) throws NoSuchFieldException {
        Field field = ReceivingStationDto.class.getDeclaredField("name");
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
    }

    @Test
    public void testEmptyName() throws NoSuchFieldException {
        nameRegex("", false);
    }

    @Test
    public void testNameWithMinimumCountOfSymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 1);
        nameRegex(name, true);
    }

    @Test
    public void testNameWithMaximumCountOfSymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 30);
        nameRegex(name, true);
    }

    @Test
    public void testNameWithTooManySymbols() throws NoSuchFieldException {
        String name = StringUtils.repeat("a", 31);
        nameRegex(name, false);
    }

    @Test
    public void testNameWithEnglishLetterInBothCases() throws NoSuchFieldException {
        nameRegex("qweQWE", true);
    }

    @Test
    public void testNameWithUkrainianLetterInBothCases() throws NoSuchFieldException {
        nameRegex("абвгґіїьяюєАБВГҐІЇЬЯЮЄ", true);
    }

    @Test
    public void testNameWithNumbers() throws NoSuchFieldException {
        nameRegex("1234567890", true);
    }

    @Test
    public void testNameWithHyphen() throws NoSuchFieldException {
        nameRegex("qwe-qwe", true);
    }

    @Test
    public void testNameWithDot() throws NoSuchFieldException {
        nameRegex("qwe.qwe", true);
    }

    @Test
    public void testNameWithWhitespace() throws NoSuchFieldException {
        nameRegex("qwe qwe", true);
    }

    @Test
    public void testNameWithApostrophe() throws NoSuchFieldException {
        nameRegex("qwe'qwe", true);
    }
}