package greencity.mapping.courier;

import greencity.dto.courier.CourierTranslationDto;
import greencity.entity.order.CourierTranslation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CourierTranslationDtoMapperTest {
    @InjectMocks
    CourierTranslationDtoMapper mapper;

    @Test
    void convert() {
        CourierTranslation expected = CourierTranslation.builder()
            .id(1L)
            .courier(null)
            .nameEng("Test")
            .name("Тест")
            .build();
        CourierTranslationDto actual = mapper.convert(expected);

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getNameEng(), actual.getNameEng());
    }
}
