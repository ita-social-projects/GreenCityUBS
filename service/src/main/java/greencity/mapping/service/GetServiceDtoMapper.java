package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.GetServiceDto;
import greencity.entity.order.Service;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class GetServiceDtoMapper extends AbstractConverter<Service, GetServiceDto> {
    @Override
    protected GetServiceDto convert(Service source) {
        return GetServiceDto.builder()
            .id(source.getId())
            .price(BigDecimal.valueOf(source.getPrice())
                .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY).doubleValue())
            .name(source.getNameUk())
            .nameEng(source.getNameEn())
            .description(source.getDescriptionUk())
            .descriptionEng(source.getDescriptionEn())
            .build();
    }
}
