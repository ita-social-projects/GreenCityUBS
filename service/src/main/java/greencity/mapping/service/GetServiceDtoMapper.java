package greencity.mapping.service;

import greencity.dto.service.GetServiceDto;
import greencity.entity.order.Service;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class GetServiceDtoMapper extends AbstractConverter<Service, GetServiceDto> {
    @Override
    protected GetServiceDto convert(Service source) {
        return GetServiceDto.builder()
            .id(source.getId())
            .price(source.getPrice())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .build();
    }
}
