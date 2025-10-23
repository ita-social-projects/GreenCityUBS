package greencity.filters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        return stringList != null ? String.join(SPLIT_CHAR, stringList) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null || string.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(string.split(SPLIT_CHAR))
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
    }
}
