package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.dto.courier.CourierUpdateDto;
import greencity.entity.order.Courier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UpdateCourierDtoMapperTest {
    @InjectMocks
    UpdateCourierDtoMapper updateCourierDtoMapper;

    @Test
    void convert() {
        CourierUpdateDto expected = ModelUtils.UPDATE_COURIER_DTO;
        Courier actual = ModelUtils.getCourier();

        assertEquals(expected.getCourierId(), updateCourierDtoMapper.convert(actual).getCourierId());
        assertEquals(expected.getNameUk(), updateCourierDtoMapper.convert(actual).getNameUk());
        assertEquals(expected.getNameEn(), updateCourierDtoMapper.convert(actual).getNameEn());
    }
}
