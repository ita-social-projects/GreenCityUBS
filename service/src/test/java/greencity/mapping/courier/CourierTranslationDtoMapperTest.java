package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
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
        CourierDto expected = CourierDto.builder()
            .courierId(1L)
            .nameEn("Test")
            .nameUk("Тест")
            .build();
        CourierTranslationDto actual = mapper.convert(expected);

        assertEquals(expected.getNameUk(), actual.getNameUk());
        assertEquals(expected.getNameEn(), actual.getNameEn());
    }
}
