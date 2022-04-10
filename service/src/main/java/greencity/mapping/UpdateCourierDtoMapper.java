package greencity.mapping;

import greencity.dto.UpdateCourierDto;
import greencity.dto.CourierTranslationDto;
import greencity.entity.order.Courier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UpdateCourierDtoMapper extends AbstractConverter<Courier, UpdateCourierDto> {
    @Override
    protected UpdateCourierDto convert(Courier source) {
        return UpdateCourierDto.builder()
            .courierId(source.getId())
            .courierTranslationDtos(source.getCourierTranslationList().stream()
                .map(courierTranslation -> CourierTranslationDto.builder()
                    .languageCode(courierTranslation.getLanguage().getCode())
                    .name(courierTranslation.getName())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
