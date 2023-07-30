package greencity.dto.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileUpdateDtoTest {
    void checkRegexPattern(String fieldName, String name, boolean validates) throws NoSuchFieldException {
        Field field = UserProfileUpdateDto.class.getDeclaredField(fieldName);
        javax.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Pattern.class);
        assertEquals(name.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"John Doe", "абвгґіїьяюєАБВГҐІЇЬЯЮЄ-ʼ'`ʹ", "John-Doe", "John Doe12", "John Doe 12", "Johnʼ'`ʹDoe",
            "ValidNameWithMaxLengthEquals30"})
    void testValidRecipientName(String name) throws NoSuchFieldException {
        checkRegexPattern("recipientName", name, true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"InvalidLengthOfName31Characters", "", "   ", "!?+=@#$%^&*", "тест!", "тест@123", "Hello World!"})
    void testInvalidRecipientName(String name) throws NoSuchFieldException {
        checkRegexPattern("recipientName", name, false);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"John Doe", "абвгґіїьяюєАБВГҐІЇЬЯЮЄ-ʼ'`ʹ", "John-Doe", "John Doe12", "John Doe 12", "Johnʼ'`ʹDoe",
            "ValidNameWithMaxLengthEquals30"})
    void testValidRecipientSurname(String name) throws NoSuchFieldException {
        checkRegexPattern("recipientSurname", name, true);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"InvalidLengthOfName31Characters", "", "   ", "!?+=@#$%^&*", "тест!", "тест@123", "Hello World!"})
    void testInvalidRecipientSurname(String name) throws NoSuchFieldException {
        checkRegexPattern("recipientSurname", name, false);
    }
}
