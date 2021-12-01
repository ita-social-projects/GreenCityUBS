package greencity.mapping;

import greencity.dto.CreateCourierDto;
import greencity.dto.CreateCourierTranslationDto;
import greencity.entity.order.Courier;
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
        List<CreateCourierTranslationDto> dtos = courierTranslations.stream().map(
            i -> new CreateCourierTranslationDto(i.getName(), i.getLanguage().getId(), i.getLimitDescription()))
            .collect(Collectors.toList());
        return CreateCourierDto.builder()
            .locationId(source.getLocation().getId())
            .maxAmountOfBigBags(source.getMaxAmountOfBigBags())
            .minAmountOfBigBags(source.getMinAmountOfBigBags())
            .maxPriceOfOrder(source.getMaxPriceOfOrder())
            .minPriceOfOrder(source.getMinPriceOfOrder())
            .createCourierTranslationDtos(dtos)
            .build();
    }
}
