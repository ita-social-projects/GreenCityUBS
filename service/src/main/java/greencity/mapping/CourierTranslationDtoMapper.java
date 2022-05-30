package greencity.mapping;

import greencity.constant.ErrorMessage;
import greencity.dto.courier.CourierTranslationDto;
import greencity.entity.order.CourierTranslation;
import greencity.exceptions.NotFoundException;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CourierTranslationDtoMapper extends AbstractConverter<CourierTranslation, CourierTranslationDto> {
    @Override
    protected CourierTranslationDto convert(CourierTranslation source) {
        return CourierTranslationDto.builder()
            .languageCode(Optional.ofNullable(source.getLanguage())
                .map(language -> language.getCode())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CANNOT_FIND_LANGUAGE_OF_TRANSLATION)))
            .name(source.getName())
            .build();
    }
}
