package greencity.mapping;

import greencity.constant.ErrorMessage;
import greencity.dto.courier.CreateCourierDto;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import greencity.exceptions.NotFoundException;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreateCourierDtoMapper extends AbstractConverter<Courier, CreateCourierDto> {
    @Override
    protected CreateCourierDto convert(Courier source) {
        List<CourierTranslation> courierTranslations = source.getCourierTranslationList();

        String en = courierTranslations.stream().filter(translation -> translation.getLanguage().getCode().equals("en"))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE)).getName();

        String ua = courierTranslations.stream().filter(translation -> translation.getLanguage().getCode().equals("ua"))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(ErrorMessage.LANGUAGE_IS_NOT_FOUND_BY_CODE)).getName();

        return CreateCourierDto.builder()
            .nameEn(en)
            .nameUa(ua)
            .build();
    }
}
