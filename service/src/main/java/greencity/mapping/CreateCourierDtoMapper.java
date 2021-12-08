package greencity.mapping;

import greencity.dto.*;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierLocations;
import greencity.entity.order.CourierTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateCourierDtoMapper extends AbstractConverter<Courier, CreateCourierDto> {
    @Override
    protected CreateCourierDto convert(Courier source) {
        List<CourierTranslation> courierTranslations = new ArrayList<>(source.getCourierTranslationList());
        List<CreateCourierTranslationDto> dtos = courierTranslations.stream().map(i -> new CreateCourierTranslationDto(
            i.getName(), i.getLanguage().getId(), i.getLimitDescription())).collect(Collectors.toList());
        List<CourierLocations> courierLocations = new ArrayList<>(source.getCourierLocations());
        List<LimitsDto> limitsDtos = courierLocations.stream().map(
            i -> new LimitsDto(i.getMinAmountOfBigBags(), i.getMaxAmountOfBigBags(), i.getMinPriceOfOrder(),
                i.getMaxPriceOfOrder(), i.getLocation().getId()))
            .collect(Collectors.toList());
        return CreateCourierDto.builder()
            .createCourierLimitsDto(limitsDtos)
            .createCourierTranslationDtos(dtos)
            .build();
    }
}
