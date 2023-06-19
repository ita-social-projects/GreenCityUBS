package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.GetServiceDto;
import greencity.entity.order.Service;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class GetServiceDtoToServiceMapper extends AbstractConverter<GetServiceDto, Service> {
    @Override
    protected Service convert(GetServiceDto source) {
        return Service.builder()
            .id(source.getId())
            .price(BigDecimal.valueOf(source.getPrice())
                .movePointRight(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
                .setScale(AppConstant.NO_DECIMALS_AFTER_POINT_IN_CURRENCY, RoundingMode.HALF_UP)
                .longValue())
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .build();
    }
}
