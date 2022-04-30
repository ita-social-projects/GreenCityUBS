package greencity.mapping;

import greencity.dto.service.ServiceTranslationDto;
import greencity.entity.order.ServiceTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class SerivceTransaltionDtoMapper extends AbstractConverter<ServiceTranslation, ServiceTranslationDto> {
    @Override
    protected ServiceTranslationDto convert(ServiceTranslation source) {
        return ServiceTranslationDto.builder()
            .name(source.getName())
            .description(source.getDescription())
            .languageId(source.getLanguage().getId())
            .build();
    }
}
