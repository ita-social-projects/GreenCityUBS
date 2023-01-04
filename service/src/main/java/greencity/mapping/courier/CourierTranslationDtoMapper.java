package greencity.mapping.courier;

import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierTranslationDto;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CourierTranslationDtoMapper extends AbstractConverter<CourierDto, CourierTranslationDto> {
    @Override
    protected CourierTranslationDto convert(CourierDto source) {
        return CourierTranslationDto.builder()
            .nameEn(source.getNameEn())
            .nameUk(source.getNameUk())
            .build();
    }
}
