package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.CourierUpdateDto;
import greencity.entity.order.Courier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCourierDtoMapperTest {
    @InjectMocks
    UpdateCourierDtoMapper updateCourierDtoMapper;

    @Test
    void convert() {
        CourierUpdateDto expected = ModelUtils.UPDATE_COURIER_DTO;
        Courier actual = ModelUtils.getCourier();

        assertEquals(expected.getCourierId(), updateCourierDtoMapper.convert(actual).getCourierId());
        assertEquals(expected.getCourierTranslationDtos(),
            updateCourierDtoMapper.convert(actual).getCourierTranslationDtos());
    }
}
