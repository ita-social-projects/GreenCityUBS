package greencity.mapping.service;

import greencity.constant.AppConstant;
import greencity.dto.service.TariffServiceDto;
import greencity.entity.order.Bag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class TariffServiceDtoMapper extends AbstractConverter<Bag, TariffServiceDto> {
    @Override
    protected TariffServiceDto convert(Bag source) {
        return TariffServiceDto.builder()
            .capacity(source.getCapacity())
            .price(convertIntoBills(source.getPrice()))
            .commission(convertIntoBills(source.getCommission()))
            .name(source.getName())
            .nameEng(source.getNameEng())
            .description(source.getDescription())
            .descriptionEng(source.getDescriptionEng())
            .build();
    }

    private Double convertIntoBills(Long coins) {
        return BigDecimal.valueOf(coins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .doubleValue();
    }
}
