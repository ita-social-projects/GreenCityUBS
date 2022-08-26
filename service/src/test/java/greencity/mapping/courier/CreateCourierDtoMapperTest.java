package greencity.mapping.courier;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.courier.CreateCourierDto;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import greencity.exceptions.NotFoundException;
import greencity.mapping.courier.CreateCourierDtoMapper;
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

        String en = courierTranslations.stream().filter(translation -> translation.getLanguage().getCode().equals("en"))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE)).getName();

        String ua = courierTranslations.stream().filter(translation -> translation.getLanguage().getCode().equals("ua"))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE)).getName();

        CreateCourierDto createCourierDto = mapper.convert(courier);
        Assertions.assertEquals(createCourierDto.getNameUa(), ua);
    }
}
