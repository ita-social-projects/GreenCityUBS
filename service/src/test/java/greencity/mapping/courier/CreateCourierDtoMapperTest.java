package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.dto.courier.CreateCourierDto;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CreateCourierDtoMapperTest {
    @InjectMocks
    private CreateCourierDtoMapper mapper;

    @Test
    void convert() {
        Courier courier = ModelUtils.getCourier();
        CreateCourierDto dto = ModelUtils.getCreateCourierDto();

        List<CourierTranslation> courierTranslations = courier.getCourierTranslationList();

        String en = courierTranslations.stream().findFirst().get().getNameEng();

        String ua = courierTranslations.stream().findFirst().get().getName();

        CreateCourierDto createCourierDto = mapper.convert(courier);
        Assertions.assertEquals(createCourierDto.getNameUa(), ua);
        Assertions.assertEquals(createCourierDto.getNameEn(), en);
    }
}
