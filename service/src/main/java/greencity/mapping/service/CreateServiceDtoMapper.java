package greencity.mapping.service;

import greencity.dto.service.CreateServiceDto;
import greencity.entity.order.Service;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class CreateServiceDtoMapper extends AbstractConverter<Service, CreateServiceDto> {
    @Override
    protected CreateServiceDto convert(Service source) {
        return CreateServiceDto.builder()
                .price(source.getPrice())
                .name(source.getName())
                .nameEng(source.getNameEng())
                .description(source.getDescription())
                .descriptionEng(source.getDescriptionEng())
                .tariffsInfoId(source.getTariffsInfo().getId())
                .build();
    }
}
