package greencity.mapping;

import greencity.dto.CourierTranslationDto;
import greencity.entity.order.CourierTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CourierTranslationDtoMapper extends AbstractConverter<CourierTranslation, CourierTranslationDto> {
    @Override
    protected CourierTranslationDto convert(CourierTranslation source) {
        return CourierTranslationDto.builder()
            .languageCode(source.getLanguage().getCode())
            .name(source.getName())
            .build();
    }
}
