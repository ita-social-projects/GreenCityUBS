package greencity.mapping;

import static greencity.ModelUtils.getLanguage;

import greencity.dto.CourierTranslationDto;
import greencity.entity.order.CourierTranslation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourierTranslationDtoMapperTest {
    @InjectMocks
    CourierTranslationDtoMapper mapper;

    @Test
    void convert() {
        CourierTranslation expected = CourierTranslation.builder()
            .id(1L)
            .courier(null)
            .language(getLanguage())
            .name("Тест")
            .build();
        CourierTranslationDto actual = mapper.convert(expected);

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getLanguage().getCode(), actual.getLanguageCode());
    }
}
