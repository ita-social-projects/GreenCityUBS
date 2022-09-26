package greencity.mapping.courier;

import greencity.dto.courier.CourierTranslationDto;
import greencity.entity.order.CourierTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CourierTranslationDtoMapper extends AbstractConverter<CourierTranslation, CourierTranslationDto> {
    @Override
    protected CourierTranslationDto convert(CourierTranslation source) {
        return CourierTranslationDto.builder()
            .nameEng(source.getNameEng())
            .name(source.getName())
            .build();
    }
}
