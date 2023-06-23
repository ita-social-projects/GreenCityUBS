package greencity.dto.position;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PositionWithTranslateDtoTest {

    void idValidation(Long id, boolean validates) throws NoSuchFieldException {
        Field field = PositionWithTranslateDto.class.getDeclaredField("id");
        javax.validation.constraints.Min[] annotations =
            field.getAnnotationsByType(javax.validation.constraints.Min.class);
        assertEquals(id >= annotations[0].value(), validates);
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 2, 3, 1000, 10000})
    void testValidId(Long id) throws NoSuchFieldException {
        idValidation(id, true);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -100, -1000})
    void testInvalidId(Long id) throws NoSuchFieldException {
        idValidation(id, false);
    }

    @Test
    void testValidNameMap() {
        PositionWithTranslateDto dto =
            new PositionWithTranslateDto(1L, Map.of("en", "Valid Name", "ua", "Дійсне ім'я"));
        assertEquals("Valid Name", dto.getName().get("en"));
        assertEquals("Дійсне ім'я", dto.getName().get("ua"));
    }

    @Test
    void testInvalidNameMap() {
        PositionWithTranslateDto dto = new PositionWithTranslateDto(1L, Map.of("", "Name"));
        assertTrue(dto.getName().containsKey(""));
        dto = new PositionWithTranslateDto(1L, Map.of("en", ""));
        assertTrue(dto.getName().containsValue(""));
        dto = new PositionWithTranslateDto(1L, null);
        assertNull(dto.getName());
    }
}