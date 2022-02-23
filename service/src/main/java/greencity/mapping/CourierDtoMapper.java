package greencity.mapping;

import greencity.dto.CourierDto;
import greencity.dto.CourierTranslationDto;
import greencity.dto.CreateCourierDto;
import greencity.entity.order.Courier;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourierDtoMapper extends AbstractConverter<Courier, CourierDto> {

    @Override
    protected CourierDto convert(Courier source) {
        return CourierDto.builder()
                .courierId(source.getId())
                .courierStatus(source.getCourierStatus().toString())
                .courierTranslationDtos(
                        source.getCourierTranslationList().stream()
                                .map(courierTranslation ->
                                        CourierTranslationDto.builder()
                                                .languageCode(courierTranslation.getLanguage().getCode())
                                                .name(courierTranslation.getName())
                                                .build()
                                ).collect(Collectors.toList())
                )
                .createDate(source.getCreateDate())
                .createdBy(source.getCreatedBy().getRecipientName()+ " " + source.getCreatedBy().getRecipientSurname())
                .build();
    }
}
