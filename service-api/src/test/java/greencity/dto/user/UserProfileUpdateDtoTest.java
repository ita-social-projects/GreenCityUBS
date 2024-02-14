package greencity.dto.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileUpdateDtoTest {
    void checkRegexPattern(String fieldName, String testValue, boolean validates) throws NoSuchFieldException {
        Field field = UserProfileUpdateDto.class.getDeclaredField(fieldName);
        jakarta.validation.constraints.Pattern[] annotations =
            field.getAnnotationsByType(jakarta.validation.constraints.Pattern.class);
        assertEquals(testValue.matches(annotations[0].regexp()), validates);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"John Doe", "абвгґіїьяюєёАБВГҐІЇЬЯЮЄЁ-ʼ'`ʹ", "John-Doe", "John Doe12", "John Doe 12", "Johnʼ'`ʹDoe",
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
        strings = {"John Doe", "абвгґіїьяюєёАБВГҐІЇЬЯЮЄЁ-ʼ'`ʹ", "John-Doe", "John Doe12", "John Doe 12", "Johnʼ'`ʹDoe",
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
