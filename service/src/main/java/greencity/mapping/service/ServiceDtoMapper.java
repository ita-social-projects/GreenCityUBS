package greencity.mapping.service;

import greencity.dto.service.ServiceDto;
import greencity.entity.order.Service;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ServiceDtoMapper extends AbstractConverter<Service, ServiceDto> {
    @Override
    protected ServiceDto convert(Service source) {
        return ServiceDto.builder()
            .id(source.getId())
            .price(source.getPrice())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .build();
    }
}
