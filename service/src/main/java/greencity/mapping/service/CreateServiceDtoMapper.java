package greencity.mapping.service;

import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.ServiceTranslationDto;
import greencity.entity.order.Service;
import greencity.entity.order.ServiceTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateServiceDtoMapper extends AbstractConverter<Service, CreateServiceDto> {
    @Override
    protected CreateServiceDto convert(Service source) {
        List<ServiceTranslation> serviceTranslationList = source.getServiceTranslations();
        List<ServiceTranslationDto> dtos = serviceTranslationList.stream().map(
            i -> new ServiceTranslationDto(i.getName(), i.getNameEng(), i.getDescription(), i.getDescriptionEng()))
            .collect(Collectors.toList());
        source.setFullPrice(source.getBasePrice() + source.getCommission());
        return CreateServiceDto.builder()
            .capacity(source.getCapacity())
            .commission(source.getCommission())
            .courierId(source.getCourier().getId())
            .price(source.getBasePrice())
            .serviceTranslationDtoList(dtos)
            .build();
    }
}
