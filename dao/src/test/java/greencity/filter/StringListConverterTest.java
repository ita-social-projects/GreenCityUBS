package greencity.filter;

import greencity.filters.StringListConverter;
import jakarta.persistence.AttributeConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class StringListConverterTest {
    private final AttributeConverter<List<String>, String> converter = new StringListConverter();

    @Test
    void convertToDatabaseColumnWithValidListReturnsJoinedStringTest() {
        List<String> input = Arrays.asList("image1", "image2", "image3");
        String expected = "image1;image2;image3";

        String result = converter.convertToDatabaseColumn(input);

        assertEquals(expected, result);
    }

    @Test
    void convertToDatabaseColumnWithNullListReturnsEmptyStringTest() {
        String result = converter.convertToDatabaseColumn(null);
        assertEquals("", result);
    }

    @Test
    void convertToDatabaseColumnWithEmptyListReturnsEmptyStringTest() {
        String result = converter.convertToDatabaseColumn(Collections.emptyList());
        assertEquals("", result);
    }

    @Test
    void convertToEntityAttributeWithValidStringReturnsListOfValuesTest() {
        String input = "image1;image2;image3";
        List<String> expected = Arrays.asList("image1", "image2", "image3");

        List<String> result = converter.convertToEntityAttribute(input);

        assertEquals(expected, result);
    }

    @Test
    void convertToEntityAttributeWithNullStringReturnsEmptyListTest() {
        List<String> result = converter.convertToEntityAttribute(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void convertToEntityAttributeWithBlankStringReturnsEmptyListTest() {
        List<String> result = converter.convertToEntityAttribute("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void convertToEntityAttributeWithEmptyElementsIgnoresThemTest() {
        String input = "image1;;image2; ;image3;";
        List<String> expected = Arrays.asList("image1", "image2", "image3");

        List<String> result = converter.convertToEntityAttribute(input);

        assertEquals(expected, result);
    }

    @Test
    void convertToEntityAttributeWithSingleValueReturnsSingleItemListTest() {
        String input = "image";
        List<String> expected = Collections.singletonList("image");

        List<String> result = converter.convertToEntityAttribute(input);

        assertEquals(expected, result);
    }
}
